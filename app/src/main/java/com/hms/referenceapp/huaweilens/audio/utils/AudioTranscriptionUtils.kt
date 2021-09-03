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

package com.hms.referenceapp.huaweilens.audio.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.entity.ResultSentence
import com.hms.referenceapp.huaweilens.common.Constants
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionConstants
import java.util.*
import java.util.function.Consumer
import kotlin.math.ceil
import kotlin.math.floor

object AudioTranscriptionUtils {

    /* Common Media Types are Defined
            Reference: https://www.iana.org/assignments/media-types/media-types.xhtml
         */
    val allowedMimeTypes = arrayOf(
        // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/audio-file-transcription-0000001050040089
        "audio/mpeg",
        "audio/wav",
        "audio/amr",
        "audio/m4a",
        "audio/x-m4a",
        "audio/x-wav",
        "audio/rnd.wav"
        //   "audio/x-mpegurl"
        // "audio/ogg",
        //  "audio/flac"
    )

    val allowedExtensions = arrayOf(
        "m4a",
        "amr",
        "wav"
    )

    private fun addZero(i: Int): String {
        var str = i.toString()
        if (str.length < 2) str = "0$str"
        return str
    }

    fun strActiveTime(position: Int, duration: Int): String {
        val active = ceil(position / 1000.0).toInt()  // position in seconds
        val hours = floor(active / 60.0 / 60.0).toInt()
        val minutes = floor(active / 60.0).toInt()
        val seconds = active % 60

        var str = "00:00"
        if(position in 0..duration) {
            when {
                duration in 0 until 1000 * 60 * 60 -> {
                    str = addZero(minutes) + ":" + addZero(seconds)
                }
                duration >= 1000 * 60 * 60 -> {
                    str = addZero(hours) + ":" + addZero((minutes % 60)) + ":" + addZero(seconds)
                }
            }
        }
        return str
    }

    fun setClickable(ctx: Activity, viewGroup: ViewGroup?, enabled: Boolean) {
        val childCount = viewGroup!!.childCount
        for (i in 0 until childCount) {
            val view = viewGroup.getChildAt(i)
            ctx.runOnUiThread {
                view.isEnabled = enabled
                view.isClickable = enabled
                view.isFocusable = enabled
            }
            if (view is ViewGroup) {
                setClickable(ctx, view, enabled)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun obtainFileNameWithOutExtension(given: String): String {
        val regex2 = """(.*)\.([^.]{3,4})""".toRegex()
        val res = regex2.findAll(given.toLowerCase())
        return res.first().groups[1]?.value?.capitalize().orEmpty()
    }

    fun isAudioMimeTypeValid(givenType: String?): Boolean {
        return listOf(*allowedMimeTypes).contains(givenType)
    }

    @SuppressLint("DefaultLocale")
    fun isAudioExtensionValid(givenExtension: String?): Boolean {
        return listOf(*allowedExtensions).contains(givenExtension?.toLowerCase())
    }

    fun getActiveSentence(position: Int, sentences: List<ResultSentence>?): ResultSentence? {
        var activeSentence: ResultSentence? = null
        sentences?.forEach(Consumer { sentence ->
            /* delegate active segment object when any segment's  start/end time matches with media player
                ignore if segment is a punctuation symbol
            */
            if (position >= sentence.startTime && position < sentence.endTime) {
                activeSentence = sentence
            }
        })
        return activeSentence
    }

    fun decideVisibility(obj: Any?): Int {
        return if (obj == null || obj == "") {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    private var resultTextViewTimer: Timer? = null
    private var resultTextViewTimerTask: TimerTask? = null
    fun resetTimerForResultTextViewHider(context: Activity, textView: TextView) {
        /*
                Clear text view if there is no result for 2000 seconds
                runOnUiThread method is mandatory to change UI component.
             */
        if (resultTextViewTimer != null && resultTextViewTimer is Timer && resultTextViewTimerTask != null) {
            resultTextViewTimer!!.cancel()
            resultTextViewTimerTask!!.cancel()
            resultTextViewTimer = null
            resultTextViewTimerTask = null
        }
        resultTextViewTimer = Timer()
        resultTextViewTimerTask = object : TimerTask() {
            override fun run() {
                context.runOnUiThread {
                    textView.visibility = View.INVISIBLE
                }
            }
        }
        resultTextViewTimer?.schedule(resultTextViewTimerTask, 2000)
    }

    @SuppressLint("DefaultLocale")
    fun cropSentences(str: String, lang: String): String {
        var delimiter = "."
        var splitLimit = 3
        if (lang == MLSpeechRealTimeTranscriptionConstants.LAN_ZH_CN) {
            delimiter = "，"
            splitLimit = 4
        }

        val split = str.split(delimiter)
        var ret = "..."

        if (split.size > splitLimit) {
            for (i in (split.size - splitLimit) until split.size) ret += split[i] + delimiter + " "
        } else {
            ret = str.capitalize()
        }

        return ret
    }

    fun isPermissionsCompleted(activity: Activity): Boolean {
        var ret = true
        Constants.AT_PERMISSION_LIST.forEach {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ret = false
            }
        }
        return ret
    }

    fun showErrorToast(activity: Activity) {
        activity.runOnUiThread {
            val errorToast = Toast.makeText(activity, activity.resources.getString(R.string.error_service_unavailable), Toast.LENGTH_SHORT)
            errorToast.show()
        }
    }

    fun showToast(activity: Activity, str: String) {
        activity.runOnUiThread {
            val toast = Toast.makeText(activity, str, Toast.LENGTH_SHORT)
            toast.show()
        }
    }

}