/*
 * Copyright 2020. Explore in HMS. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.hms.referenceapp.huaweilens.audio.presenter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.dialog.DeviceNotSupportedWarning
import com.hms.referenceapp.huaweilens.audio.dialog.LanguageNotSupportedWarning
import com.hms.referenceapp.huaweilens.audio.dialog.PermissionWarning
import com.hms.referenceapp.huaweilens.audio.entity.AudioFileTranscriptionConfig
import com.hms.referenceapp.huaweilens.audio.entity.RealTimeTranscriptionConfig
import com.hms.referenceapp.huaweilens.audio.entity.ResultSentence
import com.hms.referenceapp.huaweilens.audio.media.AudioPlayer
import com.hms.referenceapp.huaweilens.audio.media.AudioRecorder
import com.hms.referenceapp.huaweilens.audio.utils.*
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.common.language.LanguageActivity
import com.hms.referenceapp.huaweilens.common.translate.CustomAdapter
import com.hms.referenceapp.huaweilens.common.translate.GetLanguageArray
import com.hms.referenceapp.huaweilens.common.translate.Language
import com.hms.referenceapp.huaweilens.common.utils.SharedPrefs
import com.hms.referenceapp.huaweilens.main.fragments.AudioTranscriptionFragment
import com.hms.referenceapp.huaweilens.odt.App
import com.huawei.hms.mlsdk.aft.MLAftEvents
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftEngine
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftListener
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftResult
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftSetting
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscription
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionConfig
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionListener
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionResult
import java.io.File
import java.util.*


class AudioTranscriptionPresenter(fragment: AudioTranscriptionFragment) :
    AudioTranscriptionContract.Presenter, View.OnClickListener {

    private val logTag = "audio-transcription"
    private val atView = fragment.requireView()
    private val atActivity = fragment.requireActivity()
    private val sharedPrefs: SharedPrefs =
        SharedPrefs(atActivity)
    private var permissionDialog: AlertDialog? = null
    private var mLanguageArray: GetLanguageArray = GetLanguageArray(
        GetLanguageArray.LOCAL,
        atActivity,
        true
    )
        .sortByDownloadedModels()
    private var mLanguageArrayBuffer: MutableList<Language> = mutableListOf()
    private var languageNotSupportedDialog: AlertDialog? = null
    private var deviceNotSupportedDialog: AlertDialog? = null

    private lateinit var aftConfig: MLRemoteAftSetting
    private lateinit var aftEngine: MLRemoteAftEngine
    private lateinit var shortAftLoadingLayout: LinearLayout
    private lateinit var shortAftAbortButton: TextView
    private var aftTaskId: String? = null
    private var abortedTaskIds: MutableList<String> = mutableListOf()
    private lateinit var longAftLoadingLayout: LinearLayout
    private lateinit var longAftProgressBar: ProgressBar
    private lateinit var longAftProgressText: TextView
    private lateinit var longAftRandomText: TextView
    private lateinit var longAftAbortButton: TextView
    private var longAftTimer: Timer? = null
    private var longAftTimerTask: TimerTask? = null
    private var longAftUploadTimer: Timer? = null
    private var longAftRandomSegmentTimer: Timer? = null

    private var mSpeechRecognizer: MLSpeechRealTimeTranscription? = null
    private lateinit var rttConfig: MLSpeechRealTimeTranscriptionConfig

    private lateinit var listenLanguageSpinner: Spinner
    private lateinit var translateLanguageSpinner: Spinner

    private var recognizedSentences: MutableList<ResultSentence>? = mutableListOf()
    private var recognizedSentencesBuffer: MutableList<ResultSentence>? = mutableListOf()
    private var recognizedSentencesTextArray: Array<String?> = emptyArray()
    private var translatedSentences: MutableList<ResultSentence>? = mutableListOf()
    private var presentedSentences: MutableList<ResultSentence>? = mutableListOf()

    private lateinit var fullResultButton: AppCompatButton
    private lateinit var resultListView: ListView
    private lateinit var resultFullScrollView: ScrollView
    private lateinit var resultSyncedScrollView: ScrollView
    private lateinit var transcriptionResultTextView: TextView

    private lateinit var viewGroupAudioInputButtons: ViewGroup
    private lateinit var viewGroupPlayer: ViewGroup
    private lateinit var viewGroupIcons: ViewGroup

    private lateinit var audioPlayer: AudioPlayer
    private var audioRecorder: AudioRecorder? = null
    private lateinit var recordButton: ImageButton
    private lateinit var playPauseButton: ImageButton
    private lateinit var playerSeekBar: SeekBar
    private lateinit var playerActiveTime: TextView
    private lateinit var playerFile: TextView

    private var playerFileUri: Uri? = null
    private var playerFileUriBuffer: Uri? = null
    private var isDragging = false
    private lateinit var exportButton: ImageButton
    private lateinit var languageActivityButton: ImageButton

    private var isTranslationMode: Boolean = false
    private var isSourceLanguageSelectedForTranslation: Boolean = false
    private lateinit var translationLayout: LinearLayout

    override fun init() {

        //set HMS ML Kit API KEY
        MLApplication.getInstance().apiKey = App.API_KEY

        // Result Related Elements
        shortAftLoadingLayout = atView.findViewById(R.id.short_aft_loading_layout)
        shortAftAbortButton = atView.findViewById(R.id.short_aft_abort_button)

        longAftLoadingLayout = atView.findViewById(R.id.long_aft_loading_layout)
        longAftProgressBar = atView.findViewById(R.id.long_aft_upload_progressbar)
        longAftProgressText = atView.findViewById(R.id.long_aft_progress_text)
        longAftAbortButton = atView.findViewById(R.id.long_aft_abort_button)
        longAftRandomText = atView.findViewById(R.id.aft_random_segment)

        transcriptionResultTextView = atView.findViewById(R.id.transcription_result_text)
        resultListView = atView.findViewById(R.id.result_list_view)
        resultFullScrollView = atView.findViewById(R.id.result_full_scrollview)
        resultSyncedScrollView = atView.findViewById(R.id.result_synced_scrollview)

        // Find Buttons
        recordButton = atView.findViewById(R.id.record_button)
        playPauseButton = atView.findViewById(R.id.play_control_button)
        val rewindButton = atView.findViewById<ImageButton>(R.id.player_rewind_button)
        val forwardButton = atView.findViewById<ImageButton>(R.id.player_forward_button)
        fullResultButton = atView.findViewById(R.id.full_result_button)
        exportButton = atView.findViewById(R.id.export_button)
        languageActivityButton = atView.findViewById(R.id.imagebutton_language)

        // Player SeekBar
        playerSeekBar = atView.findViewById(R.id.player_seek_bar)
        playerActiveTime = atView.findViewById(R.id.player_active_time)

        // Disable player clickable on initial state
        viewGroupAudioInputButtons = atView.findViewById(R.id.layout_buttons)
        viewGroupPlayer = atView.findViewById(R.id.layout_player)
        viewGroupIcons = atView.findViewById(R.id.layout_icons)

        // listen language select box configuration
        listenLanguageSpinner = atView.findViewById(R.id.listen_language_selector)
        translateLanguageSpinner = atView.findViewById(R.id.translate_language_selector)

        listenLanguageSpinner.adapter = Adapters.listenLanguage(atActivity)

        //translation layout
        translationLayout = atView.findViewById(R.id.translating_layout)

        updateTranslateSpinner()

        translateLanguageSpinner.onItemSelectedListener = translateLanguageSpinnerListener
        listenLanguageSpinner.onItemSelectedListener = listenLanguageSpinnerListener

        // add seek bar listener and run it
        playerSeekBar.setOnSeekBarChangeListener(seekBarListener)
        atActivity.runOnUiThread(seekBarRunner)

        // Clickable Buttons
        recordButton.setOnClickListener(this)
        playPauseButton.setOnClickListener(this)
        rewindButton.setOnClickListener(this)
        forwardButton.setOnClickListener(this)
        fullResultButton.setOnClickListener(this)
        exportButton.setOnClickListener(this)
        shortAftAbortButton.setOnClickListener(this)
        longAftAbortButton.setOnClickListener(this)
        languageActivityButton.setOnClickListener(this)

        // Marquee effect on player file name
        playerFile = atView.findViewById(R.id.player_file)
        playerFile.isSelected = true

        // set player un clickable initially
        AudioTranscriptionUtils.setClickable(atActivity, viewGroupPlayer, false)
        translateLanguageSpinner.isEnabled = false

    }

    override fun initAudioPlayer() {
            audioPlayer = AudioPlayer().apply {
                viewGroupPlayer = atView.findViewById(R.id.layout_player)
                playPauseButton = atView.findViewById(R.id.play_control_button)
                playerDurationTimeTextView = atView.findViewById(R.id.player_duration_time)
                playerFileTextView = atView.findViewById(R.id.player_file)
                playerSeekBar = atView.findViewById(R.id.player_seek_bar)
                ctx = atActivity
            }
    }

    private fun initAudioRecorder() {
        audioRecorder = AudioRecorder(atActivity)
        audioRecorder!!.setOnInfoListener { _, what, _ ->
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                audioRecorder!!.end()
                destroyRttEngine()
                audioPlayer.prepareIt(audioRecorder!!.recordPathUri!!)
                audioPlayer.playIt()
                audioPlayer.runSeekBar(seekBarRunner)
                Toast.makeText(atActivity, "Recording Stopped", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun startShortAftEngine(uri: Uri, fileUri: Uri) {
        playerFileUriBuffer = fileUri
        aftEngine = MLRemoteAftEngine.getInstance().apply {
            init(atActivity.applicationContext)
            setAftListener(shortAftListener)
        }
        aftTaskId = aftEngine.shortRecognize(uri, aftConfig).also {
            Log.d(logTag, "startShortAftEngine: engine start command given. (TaskID=$it)")
        }
        /* SHORT AFT SPECIFIC PROCEDURES */
        shortAftLoadingLayout.visibility = View.VISIBLE
        AudioTranscriptionUtils.setClickable(atActivity, viewGroupAudioInputButtons, false)
        AudioTranscriptionUtils.setClickable(atActivity, viewGroupIcons, false)
        beginTranscriptionProcedure()
    }

    override fun startLongAftEngine(uri: Uri, fileUri: Uri) {
        playerFileUriBuffer = fileUri
        beginTranscriptionProcedure()
        aftEngine = MLRemoteAftEngine.getInstance().apply {
            init(atActivity.applicationContext)
            setAftListener(longAftListener)
        }
        aftTaskId = aftEngine.longRecognize(uri, aftConfig).also {
            Log.d(logTag, "startLongAftEngine: engine start command given. (TaskID=$it)")
        }
    }

    private fun abortAftEngine(taskId: String) {
        taskId.let {
            abortedTaskIds.add(it)
            aftEngine.destroyTask(it)
            //aftEngine.close()
            Log.d(logTag, "abortAftEngine: Task aborted. (TaskID=$taskId)")
        }
        clearTimer()
        longAftUploadTimer?.cancel()
        longAftUploadTimer = null
        atActivity.runOnUiThread {
            listenLanguageSpinner.isEnabled = true
            shortAftLoadingLayout.visibility = View.GONE
            longAftLoadingLayout.visibility = View.GONE
            exportButton.visibility = View.VISIBLE
            longAftProgressBar.progress = 0
            longAftProgressText.text = atView.resources.getString(R.string.uploading)
            longAftProgressBar.isIndeterminate = false
            transcriptionResultTextView.visibility =
                AudioTranscriptionUtils.decideVisibility(transcriptionResultTextView.text)
        }

        toggleScrollViewsByButtonState()

        // Enable Audio Input Buttons
        AudioTranscriptionUtils.setClickable(atActivity, viewGroupAudioInputButtons, true)
        AudioTranscriptionUtils.setClickable(atActivity, viewGroupIcons, true)

    }

    private fun startRttEngine() {
        mSpeechRecognizer = MLSpeechRealTimeTranscription.getInstance().apply {
            setRealTimeTranscriptionListener(rttListener)
            startRecognizing(rttConfig)
        }.also {
            beginTranscriptionProcedure()
            toggleScrollViewsByButtonState()
            if (aftTaskId != null) {
                abortAftEngine(aftTaskId!!)
            }
        }
    }

    private fun destroyRttEngine() {
        // Destroy engine
        mSpeechRecognizer?.destroy()

        // Add Buffer to the Sentence List, while recognition is incomplete
        if (!recognizedSentencesBuffer.isNullOrEmpty()) {
            recognizedSentencesBuffer?.let {
                it.last().let { s ->
                    s.text = s.text.plus("…") // add triple dots to the last buffer element
                }
                it.forEach { s ->
                    recognizedSentences?.add(s) // add all buffer into the list
                }
            }
        }
        // apply procedure
        finishTranscriptionProcedure()
        sharedPrefs.putStringValue("translated_language_code", "")
        executeTranslation()
    }

    private fun beginTranscriptionProcedure() {
        atActivity.runOnUiThread {
            listenLanguageSpinner.isEnabled = false
            transcriptionResultTextView.visibility = View.INVISIBLE
            resultSyncedScrollView.visibility = View.GONE
            resultFullScrollView.visibility = View.GONE
            fullResultButton.visibility = View.VISIBLE
            exportButton.visibility = View.INVISIBLE
            longAftProgressBar.progress = 0
            longAftProgressText.text = atView.resources.getString(R.string.uploading)
        }
    }

    private fun finishTranscriptionProcedure() {
        recognizedSentencesBuffer?.clear().also {
            updateResultListView()
        }
        playerFileUri = playerFileUriBuffer
        audioPlayer.apply {
            prepareIt(playerFileUri!!)
            playIt()
            runSeekBar(seekBarRunner)
        }
        atActivity.runOnUiThread {
            listenLanguageSpinner.isEnabled = true
            exportButton.visibility = View.VISIBLE
            shortAftLoadingLayout.visibility = View.GONE
            longAftLoadingLayout.visibility = View.GONE
            longAftProgressBar.progress = 0
            longAftProgressText.text = atView.resources.getString(R.string.uploading)
            transcriptionResultTextView.visibility =
                AudioTranscriptionUtils.decideVisibility(transcriptionResultTextView.text)
        }

        toggleScrollViewsByButtonState()

        // Enable Buttons Clickable
        AudioTranscriptionUtils.setClickable(atActivity, viewGroupAudioInputButtons, true)
        AudioTranscriptionUtils.setClickable(atActivity, viewGroupIcons, true)
        translateLanguageSpinner.isEnabled = true

        // save session information
        saveActivityState()
    }

    private fun destroyRealTimeTranscriptionProcedure() {
        mSpeechRecognizer?.destroy()
        listenLanguageSpinner.isEnabled = true
        exportButton.visibility = View.VISIBLE
        shortAftLoadingLayout.visibility = View.GONE
        longAftLoadingLayout.visibility = View.GONE
        transcriptionResultTextView.let {
            it.visibility = AudioTranscriptionUtils.decideVisibility(it.text)
        }
        toggleScrollViewsByButtonState()
        if (isTranslationMode && !isSourceLanguageSelectedForTranslation) {
            executeTranslation()
        }
    }

    private fun updateTranslateSpinner() {

        mLanguageArray.modelCheckListener = {

            if(mLanguageArrayBuffer.isNullOrEmpty() ||
                (mLanguageArrayBuffer.isNotEmpty() &&
                        !mLanguageArrayBuffer.containsAll(mLanguageArray.getLanguages()))) {

                mLanguageArrayBuffer.clear()
                mLanguageArrayBuffer.addAll(mLanguageArray.getLanguages())

                translateLanguageSpinner.visibility = View.INVISIBLE
                val mCustomAdapter = CustomAdapter(
                    atActivity,
                    R.layout.at_spinner_item,
                    mLanguageArray.getLanguages()
                )
                mCustomAdapter.setDropDownViewResource(R.layout.at_spinner_item)
                translateLanguageSpinner.adapter = mCustomAdapter
                val selectedCode = sharedPrefs.getStringValue("translate_language")
                val targetLanguage = mLanguageArray.getLanguages().let {
                        it.findLast { a-> a.iso6391 == selectedCode }
                }
                val targetLanguageIndex = mLanguageArray.getLanguages().indexOf(targetLanguage)
                if(targetLanguage!!.isDownloaded) {
                    translateLanguageSpinner.setSelection(targetLanguageIndex, false)
                }
                else {
                    translateLanguageSpinner.setSelection(0, false)
                }
            }

        }
    }

    private fun updateResultListView() {

        val sentenceCount = presentedSentences?.size!!
        recognizedSentencesTextArray = emptyArray()

        if (!presentedSentences.isNullOrEmpty()) {
            recognizedSentencesTextArray = arrayOfNulls(sentenceCount)
        }

        if (!recognizedSentencesBuffer.isNullOrEmpty()) {
            recognizedSentencesTextArray = arrayOfNulls(sentenceCount + 1)
            recognizedSentencesTextArray[recognizedSentencesTextArray.lastIndex] =
                transcriptionResultTextView.text.toString()
        }

        // add recognized sentences into array
        for (i in 0 until sentenceCount) {
            recognizedSentencesTextArray[i] = presentedSentences!![i].text
        }

        // finally create adapter and set to list view
        val resultListViewAdapter = ArrayAdapter<String>(
            atActivity,
            R.layout.at_result_list_view_layout,
            recognizedSentencesTextArray.reversed()
        )
        resultListView.adapter = resultListViewAdapter

    }

    private fun updatePresentedSentences() {
        presentedSentences?.clear()
        if (!isTranslationMode || isSourceLanguageSelectedForTranslation) {
            recognizedSentences?.forEach {
                presentedSentences?.add(it)
            }
        } else {
            translatedSentences?.forEach {
                presentedSentences?.add(it)
            }
        }
    }

    private fun toggleScrollViewsByButtonState() {
        atActivity.runOnUiThread {
            when (fullResultButton.isSelected) {
                true -> {
                    updateResultListView()
                    resultSyncedScrollView.visibility = View.GONE
                    resultFullScrollView.visibility = View.VISIBLE
                }
                false -> {
                    resultSyncedScrollView.visibility = View.VISIBLE
                    resultFullScrollView.visibility = View.GONE
                }
            }
        }
    }

    override fun saveActivityState() {
        val gson = Gson()
        sharedPrefs.putStringValue("recognized_sentences", gson.toJson(recognizedSentences))
        sharedPrefs.putStringValue("translated_sentences", gson.toJson(translatedSentences))
        sharedPrefs.putStringValue("player_uri", playerFileUri.toString())
        sharedPrefs.putIntValue("player_position", audioPlayer.currentPosition)
    }

    override fun restoreActivityState() {
        if (!sharedPrefs.getStringValue("recognized_sentences")
                .isNullOrEmpty() && !sharedPrefs.getStringValue("player_uri").isNullOrEmpty()
        ) {
            val gson = Gson()
            val mutableListTypeResultSentence =
                object : TypeToken<MutableList<ResultSentence>>() {}.type

            val sentencesA: MutableList<ResultSentence> = gson.fromJson(
                sharedPrefs.getStringValue("recognized_sentences"),
                mutableListTypeResultSentence
            )

            val sentencesB: MutableList<ResultSentence> = gson.fromJson(
                sharedPrefs.getStringValue("translated_sentences"),
                mutableListTypeResultSentence
            )

            var position = sharedPrefs.getIntValue("player_position")
            val strPath = sharedPrefs.getStringValue("player_uri")
            val uri = Uri.parse(strPath)

            if (uri != null && File(strPath!!).exists() && !sentencesA.isNullOrEmpty()) {
                if (position < 0) position = 0
                driveActivityWithResources(uri, position, sentencesA, sentencesB)
            }
        }
    }

    private fun driveActivityWithResources(
        uri: Uri,
        position: Int,
        sentencesA: MutableList<ResultSentence>,
        sentencesB: MutableList<ResultSentence>
    ) {

        recognizedSentences = sentencesA
        translatedSentences = sentencesB
        playerFileUri = uri
        audioPlayer.apply {
            prepareIt(uri)
            runSeekBar(seekBarRunner)
        }
        playerSeekBar.progress = position
        audioPlayer.seekTo(position)
        updatePresentedSentences()
        updateResultListView()
        toggleScrollViewsByButtonState()
        updatePlayerDynamicTexts(position)
        AudioTranscriptionUtils.setClickable(atActivity, viewGroupPlayer, true)
        translateLanguageSpinner.isEnabled = true
        exportButton.visibility = View.VISIBLE

        recognizedSentences?.last()!!.let {
            if(it.startTime == 0L && it.endTime == 0L) {
            // disable list view button
            fullResultButton.isSelected = true
            toggleScrollViewsByButtonState()
            fullResultButton.visibility = View.GONE
            }
        }

    }

    @SuppressLint("DefaultLocale")
    override fun restoreSharedPrefs() {
        // Set Default Shared Preferences
        if (sharedPrefs.getStringValue("listen_language").isNullOrEmpty() ||
            sharedPrefs.getIntValue(
                "is_full_result"
            ) == -1
        ) {

            fullResultButton.isSelected = false

            sharedPrefs.putStringValue("original_language", "") // this will be set with transcription result
            sharedPrefs.putStringValue("listen_language", GetLanguageConstant.iso[0]) // english as default
            sharedPrefs.putStringValue("translate_language", "") // original as default
            sharedPrefs.putIntValue("is_full_result", 0) // synchronized result as default

            listenLanguageSpinner.setSelection(0, true)
            translateLanguageSpinner.setSelection(0, true)

        } else {
            // restore transcription mode
            val preferredListenLanguage = GetLanguageConstant.iso.indexOf(
                sharedPrefs.getStringValue(
                    "listen_language"
                )
            )
            listenLanguageSpinner.setSelection(preferredListenLanguage, true)

            // restore translate language selection
            val preferredTranslateLanguage = sharedPrefs.getStringValue("translate_language")

            if (!mLanguageArray.getIsoCodes().contains(preferredTranslateLanguage)) {
                sharedPrefs.putStringValue("translate_language", "")
                translateLanguageSpinner.setSelection(0, true)
            }
            else {
                mLanguageArray.modelCheckListenerForSharedPref = {
                    val preferredTranslateLanguageIndex = mLanguageArray.getLanguages().let {
                        it.indexOf(
                            it.find { s -> s.iso6391 == preferredTranslateLanguage }
                        )
                    }
                    translateLanguageSpinner.setSelection(preferredTranslateLanguageIndex, true)
                }
            }

            // restore full result selection
            when (sharedPrefs.getIntValue("is_full_result")) {
                0 -> {
                    fullResultButton.isSelected = false
                    resultFullScrollView.visibility = View.GONE
                    resultSyncedScrollView.visibility = View.VISIBLE
                }
                1 -> {
                    fullResultButton.isSelected = true
                }
            }
            toggleScrollViewsByButtonState()

        }
    }

    private fun updatePlayerDynamicTexts(pos: Int) {
        atActivity.runOnUiThread {
            if (playerFileUri != null) {
                playerActiveTime.text =
                    AudioTranscriptionUtils.strActiveTime(pos, audioPlayer.duration)
                transcriptionResultTextView.text = AudioTranscriptionUtils.getActiveSentence(
                    pos,
                    presentedSentences
                )?.text ?: ""
            }
            transcriptionResultTextView.visibility = AudioTranscriptionUtils.decideVisibility(
                transcriptionResultTextView.text
            )
        }
    }

    private val seekBarRunner = Runnable {
        /* Keep running when player is playing
               Do not Run this when User is dragging seek bar
            */
        if (audioPlayer.isPlaying && !isDragging) {
            audioPlayer.currentPosition.let {
                playerSeekBar.progress = it
                updatePlayerDynamicTexts(it)
            }
        }
    }

    private val seekBarListener: SeekBar.OnSeekBarChangeListener =
        object : SeekBar.OnSeekBarChangeListener {
            var seekTo = 0

            override fun onProgressChanged(seekBar: SeekBar, newPosition: Int, isUser: Boolean) {
                // if user is dragging seek bar
                if (isUser && isDragging) {
                    seekTo = newPosition // save new position
                    updatePlayerDynamicTexts(seekTo)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isDragging = true //start dragging movement
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // seek player to new position when user stop dragging movement
                audioPlayer.seekTo(seekTo)
                isDragging = false
                if (seekTo >= audioPlayer.duration) audioPlayer.isPlayingCompleted = true // if user dragged until the end
                if (audioPlayer.isPlaying) audioPlayer.playIt() // continue to play if already playing
            }
        }



    private fun isTranslationNeeded(): Boolean {
        return isTranslationMode && !isSourceLanguageSelectedForTranslation
    }
    private fun isTranslateModelDownloaded(targetLanguage: Language, sourceLanguage: Language): Boolean {
        return targetLanguage.isDownloaded && sourceLanguage.isDownloaded && !recognizedSentences.isNullOrEmpty()
    }

    private fun executeTranslation() {
        if (isTranslationNeeded()) {
            val position = translateLanguageSpinner.selectedItemPosition
            val targetLanguage = mLanguageArray.getLanguages()[position]
            val sourceLanguage = mLanguageArray.getLanguages().find { it.iso6391 == sharedPrefs.getStringValue(
                "original_language") }

            if(isTranslateModelDownloaded(targetLanguage, sourceLanguage!!)) {
                val lastTranslationLanguageCode = sharedPrefs.getStringValue("translated_language_code")
                if(lastTranslationLanguageCode == null || lastTranslationLanguageCode != targetLanguage.iso6391) {
                    val translator = Translator()
                    translator.sourceLanguage = sourceLanguage.iso6391
                    translator.targetLanguage = targetLanguage.iso6391
                    translator.translateBatch(recognizedSentences)
                    translationLayout.visibility = View.VISIBLE
                    translator.listener = {
                        translatedSentences = translator.returnSentences
                        sharedPrefs.putStringValue(
                            "translated_language_code",
                            targetLanguage.iso6391
                        )
                        translationLayout.visibility = View.INVISIBLE
                        updatePresentedSentences()
                        updateResultListView()
                        updatePlayerDynamicTexts(audioPlayer.currentPosition)
                        saveActivityState()
                    }
                    translator.errorListener = {
                        translateLanguageSpinner.setSelection(0) // show original when failed
                        translationLayout.visibility = View.INVISIBLE
                        updatePresentedSentences()
                        updateResultListView()
                        updatePlayerDynamicTexts(audioPlayer.currentPosition)
                        AudioTranscriptionUtils.showToast(
                            atActivity,
                            atActivity.resources.getString(R.string.error_translation_unavailable)
                        )
                    }
                }
                else {
                    translationLayout.visibility = View.INVISIBLE
                    updatePresentedSentences()
                    updateResultListView()
                    updatePlayerDynamicTexts(audioPlayer.currentPosition)
                    saveActivityState()
                }
            }
            else {
                val intent = Intent(atActivity, LanguageActivity::class.java)
                intent.putExtra("LanguageCode", targetLanguage.iso6391)
                if (!sourceLanguage.isDownloaded){
                    intent.putExtra("sourceLanguageCode", sourceLanguage.iso6391)
                }
                atActivity.startActivity(intent)
            }
        }
    }

    private fun clearVars() {
        transcriptionResultTextView.text = ""
        translatedSentences?.clear()
        presentedSentences?.clear()
        recognizedSentences?.clear()
        recognizedSentencesBuffer?.clear()
    }

    private val shortAftListener: MLRemoteAftListener = object : MLRemoteAftListener {
        override fun onResult(taskId: String, result: MLRemoteAftResult, ext: Any?) {
            if (!abortedTaskIds.contains(taskId)) {
                Log.d(logTag, "onResult: shortAft: result obtained. (TaskID=$taskId)")

                if (result.isComplete && result.sentences != null) {
                    clearVars()
                    updateResultListView()
                    sharedPrefs.putStringValue(
                        "original_language",
                        GetLanguageConstant.iso[listenLanguageSpinner.selectedItemPosition]
                    )

                    val correlateA = CorrelateSentences(recognizedSentences).apply {
                        lang = aftConfig.languageCode
                    }

                    // loop each sentence and correlate list
                    result.sentences.forEach {
                        val sentence = ResultSentence(
                            it.text,
                            it.startTime.toLong(),
                            it.endTime.toLong()
                        )
                        correlateA.correlate(sentence)
                    }
                    translatedSentences = recognizedSentences
                    sharedPrefs.putStringValue("translated_language_code", "")
                    executeTranslation()
                    // result retrieved then apply procedure
                    updatePresentedSentences()
                    finishTranscriptionProcedure()
                } else {
                    Log.d(logTag, "onResult: shortAft: no result. (TaskID=$taskId)")
                    abortAftEngine(taskId)
                    AudioTranscriptionUtils.showToast(
                        atActivity,
                        atActivity.resources.getString(R.string.error_no_result_found)
                    )
                }
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onError(taskId: String, errorCode: Int, message: String) {
            if (!abortedTaskIds.contains(taskId)) {
                // Clear Result Segments and Hide Loading Bar
                shortAftLoadingLayout.visibility = View.GONE
                transcriptionResultTextView.visibility =
                    AudioTranscriptionUtils.decideVisibility(transcriptionResultTextView.text)
                listenLanguageSpinner.isEnabled = true
                // Enable Audio Input Buttons
                if(errorCode == 11102) {
                    AudioTranscriptionUtils.showToast(
                        atActivity,
                        atActivity.resources.getString(R.string.error_aft_language_not_supported)
                    )
                }
                else {
                    AudioTranscriptionUtils.showErrorToast(atActivity)
                }

                toggleScrollViewsByButtonState()
                Log.d(logTag, "shortAft: onError: $message [$errorCode] (TaskID=$taskId)")
                clearVars()
            }
            AudioTranscriptionUtils.setClickable(atActivity, viewGroupAudioInputButtons, true)
            AudioTranscriptionUtils.setClickable(atActivity, viewGroupIcons, true)
        }

        override fun onInitComplete(taskId: String, ext: Any) {
            // Reserved.
        }

        override fun onUploadProgress(taskId: String, progress: Double, ext: Any) {
            // Reserved.
        }

        override fun onEvent(taskId: String, eventId: Int, ext: Any) {
            // Reserved.
        }
    }

    private fun clearTimer() {
        longAftTimerTask?.cancel()
        longAftTimer?.cancel()
        longAftTimer = null
        longAftTimerTask = null
    }

    private val longAftListener: MLRemoteAftListener = object : MLRemoteAftListener {
        // random segments will be shown for animation
        val randomSegments: MutableList<String> = mutableListOf()
        var queryNum: Int = 0

        override fun onInitComplete(taskId: String, ext: Any?) {
            // Callback function called when the on-device initialization of audio file transcription is complete.
            // After the initialization is complete, call the startTask method to start audio file upload and processing.
            aftEngine.startTask(taskId)
            Log.d(logTag, "onInitComplete: task is started with (TaskID=$taskId)")
            /* LONG AFT SPECIFIC PROCEDURES */
            atActivity.runOnUiThread {
                longAftLoadingLayout.visibility = View.VISIBLE
            }
            AudioTranscriptionUtils.setClickable(atActivity, viewGroupAudioInputButtons, false)
            AudioTranscriptionUtils.setClickable(atActivity, viewGroupIcons, false)
        }

        override fun onUploadProgress(taskId: String, progress: Double, ext: Any?) {
            // reserved
            // does not return progress yet
            // 30.12.2020
            atActivity.runOnUiThread {
                longAftProgressBar.isIndeterminate = true // in determinate since no progress received
                //longAftProgressBar.progress = kotlin.math.ceil(progress / 1.0).toInt()
                if (progress.toInt() == 100) {
                    longAftProgressBar.isIndeterminate = true
                    longAftProgressText.text = atView.resources.getString(R.string.processing)
                    longAftRandomText.visibility = View.VISIBLE
                    animateRandomSegments()
                }
            }
        }

        override fun onEvent(taskId: String, eventId: Int, ext: Any?) {
            // Callback function of special events, including the stop, resume, and uploaded events of the transcription engine.
// For long audio file transcription, the result is returned by segment. You can create a thread in this method and call the MLRemoteAftEngine.getLongAftResult() method to periodically obtain the audio file transcription result.
            if (eventId == MLAftEvents.UPLOADED_EVENT) {
                // Periodically obotain the audio file transcriptin result using this method.
                queryNum = 0
                getResult(taskId)
                Log.d(logTag, "onEvent: file is uploaded. (TaskID=$taskId)")

                // set a timer when upload is finished
                // this timer will be cleared when any error or result is returned
                // this timer will be triggered as "no response"
                longAftTimer = Timer()
                longAftTimerTask = object : TimerTask() {
                    override fun run() {
                        abortAftEngine(taskId)
                        AudioTranscriptionUtils.showToast(
                            atActivity,
                            atActivity.resources.getString(R.string.error_service_has_no_response)
                        )
                        Log.d(logTag, "run: longAftTimer is finished. (TaskID=$taskId)")
                    }
                }
                longAftTimer!!.schedule(longAftTimerTask, 60000)
            }
        }

        override fun onResult(taskId: String, result: MLRemoteAftResult, ext: Any?) {
            clearTimer()
            // Obtain the transcription result notification.
            if (!abortedTaskIds.contains(taskId)) {
                if (result.isComplete) {
                    if(result.text != null) {
                        Log.d(logTag, "onResult: LongAft: full result obtained. (TaskID=$taskId)")
                        sharedPrefs.putStringValue(
                            "original_language",
                            GetLanguageConstant.iso[listenLanguageSpinner.selectedItemPosition]
                        )
                        longAftUploadTimer?.cancel()
                        longAftUploadTimer = null
                        longAftRandomSegmentTimer?.cancel()
                        longAftRandomSegmentTimer = null
                        longAftProgressBar.isIndeterminate = false
                        transcriptionResultTextView.text = ""
                        longAftRandomText.text = ""
                        longAftRandomText.visibility = View.INVISIBLE
                        translatedSentences?.clear()
                        presentedSentences?.clear()
                        recognizedSentences?.clear()
                        recognizedSentencesBuffer?.clear()
                        updateResultListView()

                        val correlateA = CorrelateSentences(recognizedSentences).apply {
                            lang = aftConfig.languageCode
                        }

                        // loop each sentence and correlate list
                        result.sentences.forEach {
                            val sentence = ResultSentence(
                                it.text,
                                it.startTime.toLong(),
                                it.endTime.toLong()
                            )
                            correlateA.correlate(sentence)
                        }
                        translatedSentences = recognizedSentences
                        sharedPrefs.putStringValue("translated_language_code", "")
                        executeTranslation()
                        // result retrieved then apply procedure
                        updatePresentedSentences()
                        finishTranscriptionProcedure()
                    } else {
                        Log.d(
                            logTag,
                            "onResult: LongAft: result is completed but text is null. (TaskID=$taskId)"
                        )
                        abortAftEngine(taskId)
                        AudioTranscriptionUtils.showToast(
                            atActivity,
                            atActivity.resources.getString(R.string.error_no_result_found)
                        )
                    }
                } else {
                    queryNum++
                    val currentSentenceSize = if(result.sentences!=null) result.sentences.size else 0
                    Log.d(
                        logTag,
                        "onResult: LongAft: Querying results (query num: $queryNum - current sentence count: $currentSentenceSize) (TaskID=$taskId)"
                    )
                    if(result.sentences != null) {
                        randomSegments.clear()
                        result.sentences.reversed().forEach {
                            if(randomSegments.size < 10) randomSegments.add(it.text)
                        }
                    }
                }
            }
        }

        private fun animateRandomSegments() {
            if (longAftRandomSegmentTimer == null) {
                longAftRandomSegmentTimer = Timer()
            }

            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    atActivity.runOnUiThread {
                        if(!randomSegments.isNullOrEmpty()) {
                            longAftRandomText.text = randomSegments.random()
                        }
                    }
                }
            }
            longAftRandomSegmentTimer!!.schedule(timerTask, 1000, 3000)
        }

        // Periodically obtain the long audio file transcription result.
        private fun getResult(taskId: String) {
            if (longAftUploadTimer == null) {
                longAftUploadTimer = Timer()
            }
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    // Query the long audio file transcription result by taskId.
                    aftEngine.getLongAftResult(taskId)
                }
            }
            longAftUploadTimer!!.schedule(timerTask, 5000, 10000)
        }

        override fun onError(taskId: String, errorCode: Int, message: String) {
            // Transcription error callback function.
            Log.d(logTag, "longAft: onError: $message ($errorCode) (TaskID=$taskId)")
            clearTimer()
            longAftUploadTimer?.cancel()
            longAftUploadTimer = null
            if (!abortedTaskIds.contains(taskId)) {
                AudioTranscriptionUtils.showErrorToast(atActivity)
                abortAftEngine(taskId)
            }
        }
    }

    private val rttListener: MLSpeechRealTimeTranscriptionListener =
        object : MLSpeechRealTimeTranscriptionListener {

            override fun onStartListening() {
                // The recorder starts to receive speech.
                clearVars()
                updateResultListView()
                sharedPrefs.putStringValue(
                    "original_language",
                    GetLanguageConstant.iso[listenLanguageSpinner.selectedItemPosition]
                )
            }

            override fun onStartingOfSpeech() {
                // The user starts to speak, that is, the speech recognizer detects that the user starts to speak.
            }

            override fun onVoiceDataReceived(data: ByteArray?, energy: Float, bundle: Bundle?) {
                // Return the original PCM stream and audio power to the user. This API is not running in the main thread, and the return result is processed in the sub-thread.
            }

            @SuppressLint("DefaultLocale")
            @Suppress("UNCHECKED_CAST")

            override fun onRecognizingResults(partialResults: Bundle?) {

                val correlateA = CorrelateSentences(recognizedSentences).apply {
                    lang = rttConfig.language
                }

                val correlateC = CorrelateSentences(recognizedSentencesBuffer).apply {
                    lang = rttConfig.language
                }

                // Update text view while recognition is ongoing
                transcriptionResultTextView.visibility = View.VISIBLE
                transcriptionResultTextView.text = AudioTranscriptionUtils
                    .cropSentences(
                        partialResults?.getString("results_recognizing")!!,
                        rttConfig.language
                    )

                // get sentence results with time offsets
                val results: ArrayList<MLSpeechRealTimeTranscriptionResult> =
                    partialResults.getSerializable(
                        "RESULTS_SENTENCE_OFFSET"
                    ) as ArrayList<MLSpeechRealTimeTranscriptionResult>

                // Reset Buffer whenever new result is received
                recognizedSentencesBuffer?.clear()

                // reset timer
                AudioTranscriptionUtils
                    .resetTimerForResultTextViewHider(atActivity, transcriptionResultTextView)

                results.forEach {
                    /*  RESULTS PARTIAL FINAL indicates recognition completed status
                        This feature will help to keep incomplete recognition in buffer
                        Incomplete recognition will not be lost
                     */
                    val sentence = ResultSentence(
                        it.text,
                        it.startTime.toLong(),
                        it.endTime.toLong()
                    )
                    when (partialResults.getBoolean("RESULTS_PARTIALFINAL")) {
                        true -> {
                            recognizedSentencesBuffer?.clear() // clear buffer when result is final
                            correlateA.correlate(sentence)
                        }
                        false -> {
                            correlateC.correlate(sentence)
                        }
                    }
                }

                translatedSentences = recognizedSentences

                // Update list view foreach result
                updateResultListView()
                // update presentation
                updatePresentedSentences()

            }

            override fun onError(error: Int, errorMessage: String?) {
                destroyRealTimeTranscriptionProcedure()
                audioRecorder?.end()
                AudioTranscriptionUtils.showErrorToast(atActivity)
                // Called when an error occurs in recognition.
                Log.d(logTag, "onError: message=$errorMessage")
            }

            override fun onState(state: Int, params: Bundle?) {
                // Notify the app status change.
                Log.d(logTag, "onState: $state")
            }

        }

    private val listenLanguageSpinnerListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                // update shared pref
                   sharedPrefs.putStringValue("listen_language", GetLanguageConstant.iso[position])
                    when (position) {
                        0 -> {
                            aftConfig =
                                AudioFileTranscriptionConfig(GetLanguageConstant.constantsAft[0]).config
                            rttConfig =
                                RealTimeTranscriptionConfig(GetLanguageConstant.constantsRtt[0]).config
                        }
                        1 -> {
                            aftConfig =
                                AudioFileTranscriptionConfig(GetLanguageConstant.constantsAft[1]).config
                            rttConfig =
                                RealTimeTranscriptionConfig(GetLanguageConstant.constantsRtt[1]).config
                        }
                        2 -> {
                            aftConfig =
                                AudioFileTranscriptionConfig(GetLanguageConstant.constantsAft[0]).config
                            rttConfig =
                                RealTimeTranscriptionConfig(GetLanguageConstant.constantsRtt[2]).config
                        }
                        3 -> {
                            aftConfig =
                                AudioFileTranscriptionConfig(GetLanguageConstant.constantsAft[0]).config
                            rttConfig =
                                RealTimeTranscriptionConfig(GetLanguageConstant.constantsRtt[3]).config
                        }
                        4 -> {
                            aftConfig =
                                AudioFileTranscriptionConfig(GetLanguageConstant.constantsAft[0]).config
                            rttConfig =
                                RealTimeTranscriptionConfig(GetLanguageConstant.constantsRtt[4]).config
                        }
                        5 -> {
                            aftConfig =
                                AudioFileTranscriptionConfig(GetLanguageConstant.constantsAft[0]).config
                            rttConfig =
                                RealTimeTranscriptionConfig(GetLanguageConstant.constantsRtt[5]).config
                        }
                    }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
              //  listenLanguageSpinner.setSelection(0, true)
            }
        }

    private val translateLanguageSpinnerListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val targetLanguage = mLanguageArray.getLanguages()[position]
                val sourceLanguage = mLanguageArray.getLanguages().find { it.iso6391 == sharedPrefs.getStringValue(
                    "original_language"
                ) }

                // update shared pref
                sharedPrefs.putStringValue("translate_language", targetLanguage.iso6391)

                if(sourceLanguage != null) isSourceLanguageSelectedForTranslation = sourceLanguage.iso6391 == targetLanguage.iso6391
                isTranslationMode = position != 0

                if (isTranslationMode && !isSourceLanguageSelectedForTranslation) {
                        executeTranslation()
                } else {
                    updatePresentedSentences()
                    updateResultListView()
                    updatePlayerDynamicTexts(audioPlayer.currentPosition)
                }

                parent?.getChildAt(0)?.let {
                    it.setBackgroundResource(0)
                    it.setPadding(0, 6, 0, 0)
                    it.findViewById<ImageView>(R.id.isDownloaded).visibility = View.GONE
                    it.findViewById<com.google.android.flexbox.FlexboxLayout>(R.id.flexBox).justifyContent = JustifyContent.CENTER
                }
                parent?.visibility = View.VISIBLE
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                translateLanguageSpinner.setSelection(0, true)
            }
        }

    override fun destroyMedia() {
        if (audioPlayer.isPlaying) {
            audioPlayer.apply {
                stop()
                release()
            }
        }
        if (null != audioRecorder && audioRecorder?.isRecording!!) {
            audioRecorder!!.apply {
                stop()
                release()
            }.also {
                File(it.recordPath).delete()
            }
        }
        audioPlayer.resetSeekBarTimer()
    }

    override fun resumeFragment() {

        mLanguageArray = GetLanguageArray(GetLanguageArray.LOCAL, atActivity, true)
            .sortByDownloadedModels()
        updateTranslateSpinner()

    }

    override fun pauseMedia() {
        if(audioPlayer.isPlaying) {
            audioPlayer.pauseIt()
        }

        if (null != audioRecorder && audioRecorder?.isRecording!!) {
            // Notify user
            AudioTranscriptionUtils.showToast(
                atActivity,
                atActivity.resources.getString(R.string.at_still_recording)
            )
        }
    }

    private fun isRealTimeLanguageSupportedByRegion() : Boolean {
        val cc: String = Locale.getDefault().country

        // reference: https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/real-time-transcription-0000001054964200
        return when(listenLanguageSpinner.selectedItemPosition) {
            0 -> true // english is supported in all regions
            1 -> (cc != "RU") // chinese is supported except russia
            2 -> (cc != "CN" && cc != "RU") // french is supported except russia and china
            3 -> (cc != "CN" && cc != "RU") // german is supported except russia and china***
            4 -> (cc != "CN" && cc != "RU") // italian is supported except russia and china***
            5 -> (cc != "CN" && cc != "RU") // spanish is supported except russia and china***
            else -> false
        }
    }

    private fun isPhoneBrandSupported() : Boolean {
        return when(listenLanguageSpinner.selectedItemPosition) {
            0 -> true // english supported on all brands
            1 -> true // chinese supported on all brands
            2 -> isPhoneHuaweiBrand() // french supported on specific brands only
            3 -> isPhoneHuaweiBrand()// german supported on specific brands only
            4 -> isPhoneHuaweiBrand() // italian supported on specific brands only
            5 -> isPhoneHuaweiBrand() // spanish supported on specific brands only
            else -> false
        }
    }

    private fun isPhoneHuaweiBrand() : Boolean {
        return (Constants.MANUFACTURER_BRAND == "HUAWEI" || Constants.MANUFACTURER_BRAND == "HONOR")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.record_button -> if (AudioTranscriptionUtils.isPermissionsCompleted(atActivity)) {
                if (isPhoneBrandSupported()) {
                    if (isRealTimeLanguageSupportedByRegion()) {
                        if (null == audioRecorder || !audioRecorder?.isRecording!!) {
                            // initialize recorder and start recognition simultaneously
                            initAudioRecorder()
                            audioRecorder?.apply {
                                prepare(R.id.record_button, R.id.record_chronometer)
                                record()
                            }.also {
                                startRttEngine()
                                if (audioPlayer.isPlaying) audioPlayer.pauseIt()
                            }
                        } else {
                            // stop recording and recognition
                            audioRecorder!!.apply {
                                end()
                            }.also {
                                playerFileUriBuffer = it.recordPathUri!!
                                destroyRttEngine()
                            }
                        }
                    } else {
                        languageNotSupportedDialog?.hide()
                        languageNotSupportedDialog =
                            LanguageNotSupportedWarning.build(atActivity).create()
                        languageNotSupportedDialog!!.show()
                    }
                } else {
                    deviceNotSupportedDialog?.hide()
                    val lang =
                        atActivity.resources.getStringArray(R.array.listen_languages)[listenLanguageSpinner.selectedItemPosition]
                    deviceNotSupportedDialog =
                        DeviceNotSupportedWarning.build(atActivity, lang).create()
                    deviceNotSupportedDialog!!.show()
                }
            } else {
                permissionDialog?.hide()
                permissionDialog = PermissionWarning.build(atActivity).create()
                permissionDialog!!.show()
            }
            R.id.play_control_button -> when {
                audioPlayer.isPlaying -> {
                    audioPlayer.pauseIt()
                }
                audioPlayer.isPlayingCompleted -> {
                    audioPlayer.replay()
                }
                else -> {
                    audioPlayer.playIt()
                }
            }
            R.id.player_rewind_button -> {
                audioPlayer.apply { seekBackward() }
                    .also { updatePlayerDynamicTexts(it.currentPosition) }
            }
            R.id.player_forward_button -> {
                audioPlayer.apply { seekForward() }
                    .also { updatePlayerDynamicTexts(it.currentPosition) }
            }
            R.id.full_result_button -> {
                fullResultButton.isSelected = !fullResultButton.isSelected
                toggleScrollViewsByButtonState()
                when (fullResultButton.isSelected) {
                    true -> {
                        sharedPrefs.putIntValue("is_full_result", 1)
                    }
                    false -> {
                        sharedPrefs.putIntValue("is_full_result", 0)
                    }
                }
            }
            R.id.export_button -> {
                // Create A Copy Of Document if Sentences Are Not Null
                if (!presentedSentences.isNullOrEmpty()) {
                    val intent = Intent(
                        atActivity,
                        com.hms.referenceapp.huaweilens.audio.view.SaverActivity::class.java
                    )
                    intent.putExtra("originalFileName", audioPlayer.fileName!!)
                    intent.putExtra(
                        "showTranslation", isTranslationNeeded()
                    )
                    atActivity.startActivity(intent)
                }
            }
            R.id.short_aft_abort_button -> {
                aftTaskId?.let { abortAftEngine(it) }
            }
            R.id.long_aft_abort_button -> {
                aftTaskId?.let { abortAftEngine(it) }
            }
            R.id.imagebutton_language -> {
                val intent = Intent(atActivity, LanguageActivity::class.java)
                atActivity.startActivity(intent)
            }
        }
    }


}