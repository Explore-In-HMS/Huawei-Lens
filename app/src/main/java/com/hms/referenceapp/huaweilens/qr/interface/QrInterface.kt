package com.hms.referenceapp.huaweilens.qr.`interface`

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap

interface QrInterface {

    interface CameraPresenter {
        fun init()
        fun checkPerms()
        fun initButtonCallbacks()
        fun onFragmentStart()
        fun onFragmentStop()
        fun onFragmentPause()
        fun onFragmentResume()
        fun onFragmentDestroy()
    }

    interface CameraView
}