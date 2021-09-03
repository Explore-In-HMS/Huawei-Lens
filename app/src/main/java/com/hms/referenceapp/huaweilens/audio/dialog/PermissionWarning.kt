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
import android.net.Uri
import android.provider.Settings
import com.hms.referenceapp.huaweilens.R
import com.huawei.hms.mlkit.common.ha.BundleKeyConstants

object PermissionWarning {
    fun build(activity: Activity): AlertDialog.Builder {
        return AlertDialog.Builder(activity)
            .setCancelable(true)
            .setTitle(activity.resources.getString(R.string.permission_required))
            .setMessage(activity.resources.getString(R.string.error_audio_trans_permission))
            .setPositiveButton(activity.resources.getString(R.string.check_permissions)) { _, _ ->
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BundleKeyConstants.AppInfo.packageName, null)
                }
                activity.startActivity(intent)
            }
    }
}