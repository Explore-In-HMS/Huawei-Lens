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
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import com.hms.referenceapp.huaweilens.R


object NoInternetConnectionError {
    fun build(activity:Activity): AlertDialog.Builder {
        val title = activity.resources.getString(R.string.error_internet_connection)
        val message = activity.resources.getString(R.string.open_internet_connection)
        return AlertDialog.Builder(activity)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(activity.resources.getString(R.string.ok)) { _, _ ->
                val intent = Intent(Settings.ACTION_SETTINGS)
                activity.startActivity(intent)
            }
    }
}