package com.hms.referenceapp.huaweilens.audio.dialog

import android.app.Activity
import android.app.AlertDialog
import com.hms.referenceapp.huaweilens.R

object LanguageNotSupportedWarning {
    fun build(activity: Activity): AlertDialog.Builder {
        return AlertDialog.Builder(activity)
            .setCancelable(true)
            .setTitle(activity.resources.getString(R.string.error_language_not_supported))
            .setMessage(activity.resources.getString(R.string.error_language_not_supported_rtt))
            .setPositiveButton(activity.resources.getString(R.string.dismiss)) { _, _ ->

            }
    }
}