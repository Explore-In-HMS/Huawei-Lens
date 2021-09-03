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

package com.hms.referenceapp.huaweilens.main.presenters

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.bcr.activity.ImagePreviewActivity
import com.hms.referenceapp.huaweilens.common.FileActivity
import com.hms.referenceapp.huaweilens.dsc.util.ChoosePictureDialog
import com.hms.referenceapp.huaweilens.dsc.view.PreviewActivity
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.fragments.DscFragment
import com.hms.referenceapp.huaweilens.main.interfaces.DscInterface
import com.hms.referenceapp.huaweilens.main.widgets.CameraXHelper
import kotlinx.android.synthetic.main.dsc_fragment.*
import kotlinx.android.synthetic.main.dsc_fragment.linearLayout3
import kotlinx.android.synthetic.main.dsc_fragment.tv_camera_torch
import java.io.ByteArrayOutputStream



class DscFragmentPresenter (var view: DscFragment) : DscInterface.CameraPresenter,

    LifecycleOwner {

    private lateinit var customCamera: CameraXHelper


    override fun checkPerms() {

        if(view.getViewActivity()?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                )
            } == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                view.requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) else {
            view.requireActivity().requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 3
            )
        }

    }

    override fun initCamera() {
        customCamera = CameraXHelper(view, view.camera_vieww)
        view.lifecycle.addObserver(customCamera)


    }

    override fun initButtonCallbacks() {


        view.iv_capture_imagee.setOnClickListener {

           view.showProgressDialog()
            customCamera.takePhoto(object : CameraXHelper.OnImageCapture{
                override fun onCaptureSuccess(bitmap: Bitmap) {
                    MenuActivity.SendImage.setBitmap(bitmap)

                    val intent = Intent(view.getViewActivity(), PreviewActivity::class.java)
                  //  PreviewActivity.bitmap=bitmap
                   // PreviewActivity.uri=getImageUri(bitmap, Bitmap.CompressFormat.PNG, 100)
                    view.dismissProgressDialog()
                    view.getViewActivity()?.startActivity(intent)




                }

                override fun onError(error: ImageCaptureException) {
                    Toast.makeText(view.requireContext(), "Capture failed.", Toast.LENGTH_LONG).show()
                     view.dismissProgressDialog()
                }
            })
        }



        view.iv_open_file_managerr.setOnClickListener {

            val intent = Intent(view.getViewActivity(), FileActivity::class.java)
            view.getViewActivity()?.startActivity(intent)

        }



        view.iv_add_imagee.setOnClickListener {
            view.showGallery()
        }



        view.linearLayout3.setOnClickListener {
            customCamera.enableTorch()
            if (customCamera.getIsTorchEnabled()) {
                it.alpha = 1f
                view.iv_camera_torchh.setImageDrawable(
                    ContextCompat.getDrawable(view.requireContext(), R.drawable.ic_lightbulb_on)
                )
                view.tv_camera_torch.text = "Touch here to turn light off"
            } else {
                it.alpha = .5f
                view.iv_camera_torchh.setImageDrawable(
                    ContextCompat.getDrawable(view.requireContext(), R.drawable.ic_lightbulb)
                )
                view.tv_camera_torch.text = "Touch here to turn light on"
            }
        }
    }


    override fun getImageUri(src: Bitmap, format: Bitmap.CompressFormat?, quality: Int): Uri {
        val os = ByteArrayOutputStream()
        src.compress(format, quality, os)
        val path: String = MediaStore.Images.Media.insertImage(
            view.getViewActivity()?.contentResolver,
            src,
            "Huawei-Lens",
            null
        )
        PreviewActivity.garbage?.add(path)


        return Uri.parse(path)
    }
    override fun getLifecycle(): Lifecycle {
        return view.lifecycle
    }






}