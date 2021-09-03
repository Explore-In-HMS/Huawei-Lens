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

package com.hms.referenceapp.huaweilens.tts

import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.widget.Toast
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.langdetect.MLLangDetectorFactory
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetectorSetting
import com.huawei.hms.mlsdk.tts.*
import java.text.BreakIterator
import java.util.*
import kotlin.collections.ArrayList
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.main.fragments.TTSFragment
import com.hms.referenceapp.huaweilens.odt.App

class TTSPresenter(var view: TTSFragment) : TTSInterface.TPresenter {

    private lateinit var mlTtsEngine: MLTtsEngine
    private lateinit var mlConfigs: MLTtsConfig
    private val sentences = ArrayList<String>()
    val tag = "Kant"
    private var i = 0
    private var k = 0
    private var j = 0
    var sizeCheck: Int = 0
    private var currentLanguage: String? = null
    private var lngResult: String? = null
    val mapping:HashMap<String, String> = HashMap()

    private var callback: MLTtsCallback?= object : MLTtsCallback {
        override fun onError(taskId: String, err: MLTtsError) {
            Log.d(tag, "onError: $err")
        }

        override fun onWarn(taskId: String, warn: MLTtsWarn) {
            Log.d(tag, "onWarn: $warn")
        }

        override fun onRangeStart(taskId: String, start: Int, end: Int) {
            mapping[taskId]?.let {
                view.listen(it) }
            k++
            Log.d("range", "onRangeStart: " + taskId + "," + start + "," + end + mapping[taskId])
        }

        override fun onAudioAvailable(taskId: String, audioFragment: MLTtsAudioFragment, offset: Int, range: Pair<Int, Int>,
                                      bundle: Bundle) {
        }

        override fun onEvent(taskId: String, eventId: Int, bundle: Bundle?) {
            when (eventId) {
                MLTtsConstants.EVENT_PLAY_START -> {
                    view.activity?.runOnUiThread {
                        view.ttsButton()?.setImageResource(R.drawable.ic_pause_white_48dp)
                        view.ttsButton()?.setOnClickListener {
                            mlTtsEngine.pause()
                            view.ttsButton()?.setImageResource(R.drawable.ic_play_arrow_white_48dp)
                        }
                    }

                }
                MLTtsConstants.EVENT_PLAY_STOP -> {
                    view.activity?.runOnUiThread {
                        view.ttsButton()?.setImageResource(R.drawable.ic_play_arrow_white_48dp)
                        view.ttsButton()?.setOnClickListener {
                            if(view.fromFile()){
                                view.sourceText()?.let { it1 -> trigger(it1) }
                            }else {
                                view.sourceText()?.let { it1 -> giveText(it1) }
                            }
                        }
                    }
                }
                MLTtsConstants.EVENT_PLAY_RESUME -> {
                    view.activity?.runOnUiThread {
                        view.ttsButton()?.setImageResource(R.drawable.ic_pause_white_48dp)
                        view.ttsButton()?.setOnClickListener {
                            mlTtsEngine.pause()
                            view.ttsButton()?.setImageResource(R.drawable.ic_play_arrow_white_48dp)
                        }
                    }
                }
                MLTtsConstants.EVENT_PLAY_PAUSE -> {
                    view.activity?.runOnUiThread {
                        view.ttsButton()?.setImageResource(R.drawable.ic_play_arrow_white_48dp)
                        view.ttsButton()?.setOnClickListener {
                            mlTtsEngine.resume()
                            view.ttsButton()?.setImageResource(R.drawable.ic_pause_white_48dp)
                        }
                    }
                }
                MLTtsConstants.EVENT_SYNTHESIS_START -> {
                    Log.d("SYNTHESIS_START", "onEvent: " + "mustart")
                }
                MLTtsConstants.EVENT_SYNTHESIS_END -> {
                    Log.d("SYNTHESIS_END", "onEvent: " + "mustend")
                }
                MLTtsConstants.EVENT_SYNTHESIS_COMPLETE -> {
                    Log.d("SYNTHESIS_COMPLETE", "onEvent: " + "mustcomplete")
                    sizeCheck ++
                    if (sizeCheck == sentences.size) {
                        if (currentLanguage != null) {
                            view.maleButtonP()?.let {
                                view.makeItClickable(it) }
                            view.femaleButtonP()?.let {
                                view.makeItClickable(it) }
                            view.femaleButtonP()?.setOnClickListener {
                                view.isFemale(true)
                                view.femaleButtonP()?.setBackgroundResource(R.drawable.buttonfemale)
                                view.isMale(false)
                                view.maleButtonP()?.setBackgroundResource(R.drawable.buttondisable)
                            }
                            view.maleButtonP()?.setOnClickListener {
                                view.isMale(true)
                                view.maleButtonP()?.setBackgroundResource(R.drawable.buttonmale)
                                view.isFemale(false)
                                view.femaleButtonP()?.setBackgroundResource(R.drawable.buttondisable)
                            }
                        }
                    }
                }

            }
        }
    }

    override fun init() {
        sizeCheck = 0
        i = 0
        MLApplication.getInstance().apiKey = App.API_KEY
        mlTtsEngine = MLTtsEngine(mlConfigs)
        mlTtsEngine.setTtsCallback(callback)
        view.useEngine(mlTtsEngine)

        while (i < sentences.size) {
            val id = mlTtsEngine.speak(sentences[i], MLTtsEngine.QUEUE_APPEND)
            mapping[id] = sentences[i]
            i++
        }
    }

    override fun giveText(txt: String) {
        k = 0
        mapping.clear()
        sentences.clear()
        val iterator = BreakIterator.getSentenceInstance(Locale.US)
        iterator.setText(txt)
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            sentences.add(txt.substring(start, end))
            start = end
            end = iterator.next()
        }
        Log.d("tag", "giveText: $sentences")
        while (j < sentences.size) {
            if (sentences[j] == "\n") {
                sentences.remove(sentences[j])
            }
            j++
        }
        Log.d("tag", "giveText: $sentences")
        if(view.fromFile()){
            importDetectLanguage(sentences[0] + sentences[1], txt)
        }else {
            manualDetectLanguage(txt)
        }
    }

    override fun trigger(txt: String) {
        if(view.fromFile()) {
            lngResult?.let {
                view.selectSpeaker(it)
            }
        } else {
            manualDetectLanguage(txt)
        }
    }

    override fun importDetectLanguage(shortTxt: String, longTxt: String) {
        val factory = MLLangDetectorFactory.getInstance()
        val setting = MLLocalLangDetectorSetting.Factory()
            .setTrustedThreshold(0.01f)
            .create()
        val mlLocalLangDetector = factory.getLocalLangDetector(setting)
        val firstBestDetectTask = mlLocalLangDetector.firstBestDetect(shortTxt)
        firstBestDetectTask.addOnSuccessListener { language ->
            Log.d("LanguageDetected", "onSuccess: $language")
            mlConfigs = MLTtsConfig()
                .setSpeed(view.setSpeed())
                .setVolume(view.setVolume())
            when (language) {
                "en" ->  { view.setMyText(longTxt)
                            lngResult = "en"}
                "zh" ->  {view.setMyText(longTxt)
                            lngResult = "zh"}
                "de" ->  {view.setMyText(longTxt)
                            lngResult = "de"}
                "es" ->  {view.setMyText(longTxt)
                            lngResult = "es"}
                "it" ->  {view.setMyText(longTxt)
                            lngResult = "it"}
                "fr" ->  {view.setMyText(longTxt)
                            lngResult = "fr"}
                else -> {
                    Toast.makeText(view.context,"The language of the document is not supported by Text-to-Speech",
                        Toast.LENGTH_LONG).show()
                }
            }
        }.addOnFailureListener {
                e -> Log.d("LanguageFail", "onFailure: $e")
            Toast.makeText(view.context,"The language of the document can not be detected.",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun manualDetectLanguage(txt: String) {
        val factory = MLLangDetectorFactory.getInstance()
        val setting = MLLocalLangDetectorSetting.Factory()
            .setTrustedThreshold(0.01f)
            .create()
        val mlLocalLangDetector = factory.getLocalLangDetector(setting)
        val firstBestDetectTask = mlLocalLangDetector.firstBestDetect(txt)
        firstBestDetectTask.addOnSuccessListener { language ->
            Log.d("LanguageDetected", "onSuccess: $language")
            mlConfigs = MLTtsConfig()
                .setSpeed(view.setSpeed())
                .setVolume(view.setVolume())
            when (language) {
                "en" -> view.selectSpeaker(language)
                "zh" -> view.selectSpeaker(language)
                "de" -> view.selectSpeaker(language)
                "es" -> view.selectSpeaker(language)
                "it" -> view.selectSpeaker(language)
                "fr" -> view.selectSpeaker(language)
                else -> {
                    view.selectSpeaker("other")
                }
            }
        }.addOnFailureListener { e -> Log.d("LanguageFail", "onFailure: $e") }
    }

    override fun setConfigs(lng: String, gender: String ) {
        if (lng == "en") {
            currentLanguage = "en"
            if(gender == "male"){
                mlConfigs.setLanguage(Timbres.TTS_EN_US).person = Timbres.TTS_SPEAKER_MALE_EN
            } else if (gender == "female") {
                mlConfigs.setLanguage(Timbres.TTS_EN_US).person = Timbres.TTS_SPEAKER_FEMALE_EN
            }
        } else if (lng == "zh"){
            currentLanguage = "zh"
            if(gender == "male"){
                mlConfigs.setLanguage(Timbres.TTS_ZH).person = Timbres.TTS_SPEAKER_MALE_ZH
            } else if (gender == "female") {
                mlConfigs.setLanguage(Timbres.TTS_ZH).person = Timbres.TTS_SPEAKER_FEMALE_ZH
            }
        }
        when (lng) {
            "de" -> {mlConfigs.setLanguage(Timbres.TTS_DE).person = Timbres.TTS_SPEAKER_FEMALE_DE
                    currentLanguage = null }
            "es" -> {mlConfigs.setLanguage(Timbres.TTS_ES).person = Timbres.TTS_SPEAKER_FEMALE_ES
                    currentLanguage = null }
            "it" -> {mlConfigs.setLanguage(Timbres.TTS_IT).person = Timbres.TTS_SPEAKER_FEMALE_IT
                    currentLanguage = null }
            "fr" -> {mlConfigs.setLanguage(Timbres.TTS_FR).person = Timbres.TTS_SPEAKER_FEMALE_FR
                    currentLanguage = null }
        }
        init()
    }

    override fun giveCurrentLng(): String? {
        return currentLanguage
    }

}