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

package com.hms.referenceapp.huaweilens.main.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.dialog.FileFormatError
import com.hms.referenceapp.huaweilens.audio.dialog.AftLanguageNotsupported
import com.hms.referenceapp.huaweilens.audio.dialog.PermissionWarning
import com.hms.referenceapp.huaweilens.audio.presenter.AudioTranscriptionContract
import com.hms.referenceapp.huaweilens.audio.presenter.AudioTranscriptionPresenter
import com.hms.referenceapp.huaweilens.audio.utils.AudioTranscriptionUtils
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.common.FileActivity
import com.hms.referenceapp.huaweilens.common.utils.GetFilePath
import kotlin.math.floor

class AudioTranscriptionFragment : Fragment(), AudioTranscriptionContract.View {
    private lateinit var presenter: AudioTranscriptionContract.Presenter
    private var permissionDialog: AlertDialog? = null
    private var frenchNotSupportedDialog: AlertDialog? = null
    private var fileFormatDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audio_transcription, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        presenter = AudioTranscriptionPresenter(this).apply {
            initAudioPlayer()
            init()
            restoreSharedPrefs()
            restoreActivityState()
        }
        val openFolderButton = view.findViewById<ImageView>(R.id.btn_open_folder)
        val chooseFileButton = view.findViewById<ImageView>(R.id.btn_choose_file)
        val listenLanguageSpinner = view.findViewById<Spinner>(R.id.listen_language_selector)

        chooseFileButton.setOnClickListener {
            if(listenLanguageSpinner.selectedItemPosition <= 1) {
                if(AudioTranscriptionUtils.isPermissionsCompleted(activity as Activity)) {
                    fileSelectorIntent()
                }
                else {
                    permissionDialog?.hide()
                    permissionDialog = PermissionWarning.build(activity as Activity).create()
                    permissionDialog!!.show()
                }
            }
            else {
                frenchNotSupportedDialog?.hide()
                val lang = this.resources.getStringArray(R.array.listen_languages)[listenLanguageSpinner.selectedItemPosition]
                frenchNotSupportedDialog = AftLanguageNotsupported.build(activity as Activity, lang).create()
                frenchNotSupportedDialog!!.show()
            }
        }

        openFolderButton.setOnClickListener {
            val intent = Intent(activity, FileActivity::class.java)
            activity?.startActivity(intent)
        }

    }

    private val fileSelectorForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onActivityResultSelectFile(result)
    }

    private val askMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {permissions ->
        if(permissions.containsValue(false)) {
            promptPermissionRequiredDialog()
        }
        else {
            permissionDialog?.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        if(!AudioTranscriptionUtils.isPermissionsCompleted(activity as Activity) && (permissionDialog == null || (permissionDialog != null && !permissionDialog!!.isShowing))) {
            askMultiplePermissions.launch(Constants.AT_PERMISSION_LIST)
        }
        presenter.resumeFragment()
    }

    override fun onPause() {
        super.onPause()
        presenter.pauseMedia()
        presenter.saveActivityState()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroyMedia()
        presenter.saveActivityState()
    }

    override fun onActivityResultSelectFile(result: ActivityResult) {
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                processSelectedFile(data)
            }
    }

    override fun fileSelectorIntent() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "audio/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_MIME_TYPES, AudioTranscriptionUtils.allowedMimeTypes)
            }
            fileSelectorForResult.launch(Intent.createChooser(intent, requireContext().resources.getString(R.string.import_file_message)))
        } catch (ex: ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(
                activity, requireContext().resources.getString(R.string.error_install_file_manager),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun promptPermissionRequiredDialog() {
        permissionDialog?.hide()
        permissionDialog = PermissionWarning.build(activity as Activity).create()
        permissionDialog!!.show()
    }


    @SuppressLint("Recycle")
    override fun processSelectedFile(data: Intent?) {

        // Get the Uri of the selected file
        val uri: Uri = data!!.data!!
        val mimeType = activity?.contentResolver!!.getType(uri)
        val cursor = activity?.contentResolver!!
            .query(uri, arrayOf(MediaStore.Audio.AudioColumns.SIZE), null, null, null)!!
            .also {
                it.moveToFirst()
            }
        val fileSize = cursor.getLong(0)
        val extension: String = GetFilePath()
            .getPath(activity, uri)?.substringAfterLast(".")?: "N/A"
        cursor.close()
        // check mime type and extension
        if (AudioTranscriptionUtils.isAudioMimeTypeValid(mimeType) && AudioTranscriptionUtils.isAudioExtensionValid(extension)) {
            val retriever = MediaMetadataRetriever().also {
                it.setDataSource(activity, uri)
            }
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()
            val fileUri = Uri.parse(
                GetFilePath()
                    .getPath(activity, uri))
            /*  Check duration limit for SHORT / LONG TRANSCRIPTION
                Check file size limit for SHORT / LONG TRANSCRIPTION
             */
            if (duration != null && duration < Constants.SHORT_AFT_MAX_DURATION_LIMIT_MILLIS) {
                when {
                    fileSize < Constants.SHORT_AFT_MAX_FILE_SIZE_LIMIT_BYTES -> {
                        presenter.startShortAftEngine(uri, fileUri)
                    }
                    fileSize in Constants.SHORT_AFT_MAX_FILE_SIZE_LIMIT_BYTES..Constants.LONG_AFT_MAX_FILE_SIZE_LIMIT_BYTES -> {
                        presenter.startLongAftEngine(uri, fileUri) // try to switch long recognize if file size is bigger than limit
                    }
                    else -> {
                        fileFormatDialog?.hide()
                        fileFormatDialog = FileFormatError(activity as Activity).apply {
                            title = "File Size Exceeds " + Constants.LONG_AFT_MAX_FILE_SIZE_LIMIT_BYTES / 1000 / 1000 + " MB."
                        }.build().create()
                        fileFormatDialog!!.show()
                    }
                }
            }
            else if (duration != null && duration in Constants.SHORT_AFT_MAX_DURATION_LIMIT_MILLIS..Constants.LONG_AFT_MAX_DURATION_LIMIT_MILLIS) {
                when {
                    fileSize <= Constants.LONG_AFT_MAX_FILE_SIZE_LIMIT_BYTES -> {
                        presenter.startLongAftEngine(uri, fileUri)
                    }
                    else -> {
                        fileFormatDialog?.hide()
                        fileFormatDialog = FileFormatError(activity as Activity).apply {
                            title = "File Size Exceeds " + Constants.LONG_AFT_MAX_FILE_SIZE_LIMIT_BYTES / 1000 / 1000 + " MB."
                        }.build().create()
                        fileFormatDialog!!.show()
                        //Toast.makeText(activity, "File Size Exceeds " + Constants.LONG_AFT_MAX_FILE_SIZE_LIMIT_BYTES / 1000 / 1000 + " MB.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else {
                fileFormatDialog?.hide()
                var limit: String = (Constants.LONG_AFT_MAX_DURATION_LIMIT_MILLIS / 1000 / 60 / 60.0).toString()
                var unit = "hours"
                if(limit.toDouble() < 1) {
                    limit = (floor(limit.toDouble() * 60)).toString()
                    unit = "minutes"
                }

                fileFormatDialog = FileFormatError(activity as Activity).apply {
                    title = "File Duration Exceeds ${limit.substringBefore(".")} $unit"
                }.build().create()
                fileFormatDialog!!.show()

            }
        } else {
            fileFormatDialog?.hide()
            fileFormatDialog = FileFormatError(activity as Activity).apply {
                title = "File Not Supported. ($extension)"
            }.build().create()
            fileFormatDialog!!.show()
        }
    }


}