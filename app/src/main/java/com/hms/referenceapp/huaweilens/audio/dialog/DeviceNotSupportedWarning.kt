package com.hms.referenceapp.huaweilens.audio.dialog

import android.app.Activity
import android.app.AlertDialog
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.Constants

object DeviceNotSupportedWarning {
    fun build(activity: Activity, language: String): AlertDialog.Builder {
        return AlertDialog.Builder(activity)
            .setCancelable(true)
            .setTitle(activity.resources.getString(R.string.error_language_not_supported))
            .setMessage(activity.resources.getString(R.string.error_rtt_not_available_for_device, language, Constants.MANUFACTURER_BRAND))
            .setPositiveButton(activity.resources.getString(R.string.dismiss)) { _, _ ->

            }
    }
}