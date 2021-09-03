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

package com.hms.referenceapp.huaweilens.audio.media

import android.app.Activity
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.ImageButton
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.Constants
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorder(private var ctx: Context) : MediaRecorder() {
    private var audioRecordFileName: String

    private lateinit var recordButton: ImageButton
    private lateinit var recordChronometer: Chronometer
    var recordPath: String

    var recordPathUri: Uri? = null
    var isRecording = false

    init {
        // Create recording_folder if it doesn't exist
        val f = File(Constants.ROOT_FOLDER, Constants.RECORDING_FOLDER)
        if (!f.exists()) f.mkdirs()

        // Sample Date Format for unique file names: 20201225_173600
        val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.CANADA)
        val now = Date()
        audioRecordFileName = "Recording_" + formatter.format(now) + ".amr"
        // set file path
        this.recordPath = Constants.ROOT_FOLDER + Constants.RECORDING_FOLDER + "/" + audioRecordFileName
    }

    fun record() {
        this.start()
        isRecording = true
        recordButton.isPressed = true
        recordButton.setBackgroundResource(R.drawable.microphone_circle_recording)
        recordChronometer.base = SystemClock.elapsedRealtime()
        recordChronometer.start()
        recordChronometer.visibility = View.VISIBLE
    }

    fun end() {
        // switch controller variable and stop media recorder instance
        this.stop()
        this.release()
        isRecording = false
        recordButton.isPressed = false
        recordButton.setBackgroundResource(R.drawable.ic_camera_button)
        recordChronometer.stop()
        recordChronometer.visibility = View.INVISIBLE
        // create uri for processed file
        this.recordPathUri = Uri.fromFile(File(recordPath))
    }

    fun prepare(recordButtonId: Int, chronoMeterId: Int) {
        try {
            // Initialize Recorder with settings
            this.setAudioSource(AudioSource.MIC)
            this.setOutputFormat(OutputFormat.AMR_WB)
            this.setMaxDuration(Constants.RTT_MAX_DURATION_AUDIO_RECORD)
            this.setAudioChannels(2)
            this.setAudioEncodingBitRate(192)
            this.setAudioSamplingRate(44100)
            this.setOutputFile(recordPath)
            this.setAudioEncoder(AudioEncoder.AMR_WB)
            this.prepare()
            this.recordButton = (this.ctx as Activity).findViewById(recordButtonId)
            this.recordChronometer = (this.ctx as Activity).findViewById(chronoMeterId)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}