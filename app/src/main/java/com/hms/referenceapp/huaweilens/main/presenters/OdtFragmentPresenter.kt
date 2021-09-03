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

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.language.LanguageActivity
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.fragments.OdtFragment
import com.hms.referenceapp.huaweilens.main.interfaces.OdtFragmentInterface
import com.hms.referenceapp.huaweilens.main.widgets.CameraXHelper
import com.hms.referenceapp.huaweilens.odt.App
import com.hms.referenceapp.huaweilens.odt.activities.ClassificationActivity
import kotlinx.android.synthetic.main.fragment_odt.*

class OdtFragmentPresenter(var view: OdtFragment) : OdtFragmentInterface.CameraPresenter,
    LifecycleOwner {

    private lateinit var customCamera: CameraXHelper

    override fun initCamera() {
        customCamera = CameraXHelper(view, view.camera_view)
        view.lifecycle.addObserver(customCamera)
    }

    @SuppressLint("SetTextI18n")
    override fun initButtonCallbacks() {
        view.iv_capture_image.setOnClickListener {
            val progressDialog = ProgressDialog(view.requireContext())
            // progressDialog.setTitle("In Progress")
            progressDialog.setCanceledOnTouchOutside(false)
            //progressDialog?.rotation = 90F
            progressDialog.setMessage("Image Preview is loading, please wait")
            progressDialog.show()

            customCamera.takePhoto(object : CameraXHelper.OnImageCapture {


                override fun onCaptureSuccess(bitmap: Bitmap) {
                    MenuActivity.SendImage.setBitmap(bitmap)
                    progressDialog.dismiss()
                    val intent = Intent(view.requireActivity(), ClassificationActivity::class.java)
                    view.startActivity(intent)
                }

                override fun onError(error: ImageCaptureException) {
                    Toast.makeText(
                        view.requireContext(),
                        "An error has occurred $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        view.linearLayout3.setOnClickListener {
            customCamera.enableTorch()
            if (customCamera.getIsTorchEnabled()) {
                it.alpha = 1f
                view.iv_camera_torch.setImageDrawable(
                    ContextCompat.getDrawable(view.requireContext(), R.drawable.ic_lightbulb_on)
                )
                view.tv_camera_torch.text = "Touch here to turn light off"
            } else {
                it.alpha = .5f
                view.iv_camera_torch.setImageDrawable(
                    ContextCompat.getDrawable(view.requireContext(), R.drawable.ic_lightbulb)
                )
                view.tv_camera_torch.text = "Touch here to turn light on"
            }
        }
        view.iv_add_image.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            view.startForResult.launch(intent)
        }
        view.imagebutton_language.setOnClickListener{
            val intent = Intent(view.requireActivity(), LanguageActivity::class.java)
            view.startActivity(intent)
        }
    }

    override fun getLifecycle(): Lifecycle {
        return view.lifecycle
    }

}