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
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.bcr.BusinessCardRecognition
import com.hms.referenceapp.huaweilens.bcr.activity.ImagePreviewActivity
import com.hms.referenceapp.huaweilens.bcr.util.BitmapUtils
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.interfaces.BcrFragmentInterface
import com.hms.referenceapp.huaweilens.main.widgets.CameraXHelper
import kotlinx.android.synthetic.main.fragment_business_card.*
import kotlinx.android.synthetic.main.fragment_odt.linearLayout3
import kotlinx.android.synthetic.main.fragment_odt.tv_camera_torch


class BcrFragmentPresenter(var view: BusinessCardRecognition) :
    BcrFragmentInterface.BcrPresenter,
    LifecycleOwner {

    private lateinit var customCamera: CameraXHelper
    var sensorManager: SensorManager? = null
    var sensor: Sensor? = null
    var sensorEventListener: SensorEventListener? = null
    private lateinit var animation: Animation
    private  var ctrl:Boolean=true

    override fun initCamera() {
        customCamera = CameraXHelper(view, view.camera_view)
        view.lifecycle.addObserver(customCamera)
        ctrl=true
        animation= AnimationUtils.loadAnimation(view.requireActivity(), R.anim.anim_blink)
        view.bcr_rotate_left.startAnimation(animation)
        initSensor()
        registerSensor()
    }



    override fun initButtonCallbacks() {
        view.iv_capture_image.setOnClickListener {

            view.camera_view.visibility = View.GONE
            view.showProgressDialog()


            customCamera.takePhoto(object : CameraXHelper.OnImageCapture {
                override fun onCaptureSuccess(bitmap: Bitmap) {

                    val bitmapRotated = bitmap.rotate(-90F)
                    MenuActivity.SendImage.setBitmap(bitmapRotated)
                    val intent = Intent(view.requireActivity(), ImagePreviewActivity::class.java)
                    view.requireActivity().startActivity(intent)
                    view.dismissProgressDialog()
                    view.camera_view.visibility = View.VISIBLE
                //    view.camera_image_preview.visibility = View.GONE
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


        view.iv_add_image.setOnClickListener {

            val permissionCheck = ContextCompat.checkSelfPermission(
                view.requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
               view.startGallery()

            } else {
                ActivityCompat.requestPermissions(
                    view.requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    2000
                )
            }
        }


    }


    private fun initSensor() {
        var ctrl=true

        sensorManager = view.requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {

                if (event.values[0]>=7&&ctrl){
                    dismissAnim()
                    ctrl=false
                }
                else if(!ctrl&& event.values[0]<7){
                    showAnim()
                    ctrl=true
                }

            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

    }

     fun showAnim() {

        view.bcr_rotate_left.visibility=View.VISIBLE
         view.bcr_rotate_left.startAnimation(animation)


    }
     fun dismissAnim() {

         view.bcr_rotate_left.visibility=View.GONE
         view.bcr_rotate_left.clearAnimation()
    }

   override fun registerSensor() {
        sensorManager!!.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun unRegisterSensor() {
        sensorManager!!.unregisterListener(sensorEventListener)
    }



    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    override fun getLifecycle(): Lifecycle {
        return view.lifecycle
    }


    companion object {
        private const val REQUEST_IMAGE_SELECT_FROM_ALBUM = 1000
        private const val REQUEST_IMAGE_CAPTURE = 1001
    }

}