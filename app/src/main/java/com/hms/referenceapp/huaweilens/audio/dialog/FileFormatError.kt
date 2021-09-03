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

package com.hms.referenceapp.huaweilens.audio.dialog
import android.app.Activity
import android.app.AlertDialog
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.utils.AudioTranscriptionUtils
import com.hms.referenceapp.huaweilens.common.Constants
import kotlin.math.floor

class FileFormatError (var activity: Activity) {
    var title = activity.resources.getString(R.string.error_file_cannot_process)
    var message = ""

    fun build(): AlertDialog.Builder {

        val sizeLimit = (Constants.LONG_AFT_MAX_FILE_SIZE_LIMIT_BYTES / 1000 / 1000).toString()+ " MB"
        var durationLimit: String = (Constants.LONG_AFT_MAX_DURATION_LIMIT_MILLIS / 1000 / 60 / 60.0).toString()
        var durationUnit = "hours"
        if(durationLimit.toDouble() < 1) {
            durationLimit = (floor(durationLimit.toDouble() * 60)).toString()
            durationUnit = "minutes"
        }
        var extensions = ""
        AudioTranscriptionUtils.allowedExtensions.forEachIndexed { i, it ->
            extensions += ".$it"
            if(i != AudioTranscriptionUtils.allowedExtensions.lastIndex) extensions += ", "
        }

        var description = "Supported: $extensions" +
                "\nMax file duration: ${durationLimit.substringBefore(".")} $durationUnit" +
                "\nMax file size: $sizeLimit"

        return AlertDialog.Builder(activity)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message + description)
            .setPositiveButton(activity.resources.getString(R.string.try_again)) { _, _ ->

            }
    }
}