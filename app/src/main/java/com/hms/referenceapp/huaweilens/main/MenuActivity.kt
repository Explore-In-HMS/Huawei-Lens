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

package com.hms.referenceapp.huaweilens.main

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.dialog.PermissionWarning
import com.hms.referenceapp.huaweilens.audio.utils.AudioTranscriptionUtils
import com.hms.referenceapp.huaweilens.bcr.BusinessCardRecognition
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.main.adapters.FeatureAdapter
import com.hms.referenceapp.huaweilens.main.adapters.MainPagerAdapter
import com.hms.referenceapp.huaweilens.main.fragments.*
import com.hms.referenceapp.huaweilens.main.helpers.CenterDecoration
import com.hms.referenceapp.huaweilens.main.helpers.CenterSnapHelper
import com.hms.referenceapp.huaweilens.main.helpers.ZoomOutPageTransformer
import com.hms.referenceapp.huaweilens.main.interfaces.FeatureClickCallback
import com.hms.referenceapp.huaweilens.qr.QrFragment
import com.huawei.agconnect.crash.AGConnectCrash
import com.huawei.hms.mlsdk.common.MLApplication
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity(), FeatureClickCallback {
    private var permissionDialog: AlertDialog? = null
    private val adapter by lazy {
        MainPagerAdapter(
            this,
            listOf(
                QrFragment(),
                BusinessCardRecognition(),
                OdtFragment(),
                DscFragment(),
                FrFragment(),
                TRTNewFragment(),
                TTSFragment(),
                AudioTranscriptionFragment()

            )
        )
    }
    private val listImages: List<String> = listOf(
        "Qr Reader",
        "Business Card",
        "Identify",
        "Document Correction",
        "Form Recognizer",
        "Real Time Translate",
        "Text to Speech",
        "Audio Transcription"

    )
    private val snapHelper by lazy { CenterSnapHelper() }
    private val itemDecoration by lazy {
        CenterDecoration(
            50
        )
    }
    private val recyclerAdapter by lazy {
        FeatureAdapter(
            this,
            listImages,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        AGConnectCrash.getInstance().enableCrashCollection(true)
        MLApplication.getInstance().apiKey =
            "CgB6e3x9ztsd3VJtjuiA1Y3KKZvhFop97qcIeK6SieCUfTwji9HANSwK/mXeIyLncVCRn7IBwrc9HWttO5iGMdl9"
        if (!AudioTranscriptionUtils.isPermissionsCompleted(this) && (permissionDialog == null || (permissionDialog != null && !permissionDialog!!.isShowing))) {
            askMultiplePermissions.launch(Constants.AT_PERMISSION_LIST)
        } else {
            view_pager.adapter = adapter
            view_pager.offscreenPageLimit = 6
            feature_recycler_view.adapter = recyclerAdapter
            feature_recycler_view.addItemDecoration(itemDecoration)
            snapHelper.attachToRecyclerView(feature_recycler_view)
            view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {

                    if (SendImage.ctrl == 1) {
                        //dsc

                        onClicked(3)
                        recyclerAdapter.setSelectedItem(3)
                        snapHelper.scrollTo(3, true)
                        SendImage.ctrl = 0
                        recyclerAdapter.notifyDataSetChanged()


                    } else if (SendImage.ctrl == 2) {
                        //form
                        onClicked(4)
                        recyclerAdapter.setSelectedItem(4)
                        snapHelper.scrollTo(4, true)
                        SendImage.ctrl = 0
                        recyclerAdapter.notifyDataSetChanged()


                    } else {
                        recyclerAdapter.setSelectedItem(position)
                        snapHelper.scrollTo(position, true)
                        recyclerAdapter.notifyDataSetChanged()
                    }
                }
            })

            view_pager.setPageTransformer(ZoomOutPageTransformer())
        }

    }


    private val askMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.containsValue(false)) {
                promptPermissionRequiredDialog()
            } else {
                permissionDialog?.hide()
            }
        }

    private fun promptPermissionRequiredDialog() {
        permissionDialog?.hide()
        permissionDialog = PermissionWarning.build(this).create()
        permissionDialog!!.show()
    }

    override fun onResume() {
        super.onResume()
        if (!AudioTranscriptionUtils.isPermissionsCompleted(this) && (permissionDialog == null || (permissionDialog != null && !permissionDialog!!.isShowing))) {
            askMultiplePermissions.launch(Constants.AT_PERMISSION_LIST)
        }
    }

    override fun onClicked(position: Int) {
        snapHelper.scrollTo(position, true)
        view_pager.currentItem = position
    }

    class SendImage {
        companion object {
            var ctrl: Int = 0
            var info: Bitmap? = null


            fun getBitmap(): Bitmap? {
                return info
            }

            fun setBitmap(bitmap: Bitmap) {
                this.info = bitmap
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.size == 4) {
            view_pager.adapter = adapter
            view_pager.offscreenPageLimit = 8
            feature_recycler_view.adapter = recyclerAdapter
            feature_recycler_view.addItemDecoration(itemDecoration)
            snapHelper.attachToRecyclerView(feature_recycler_view)
            view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    recyclerAdapter.setSelectedItem(position)
                    snapHelper.scrollTo(position, true)
                    recyclerAdapter.notifyDataSetChanged()
                }
            })

            view_pager.setPageTransformer(ZoomOutPageTransformer())
        }
    }


}