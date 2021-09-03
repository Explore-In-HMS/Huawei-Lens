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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
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
import com.hms.referenceapp.huaweilens.dsc.view.PreviewActivity
import com.hms.referenceapp.huaweilens.fr.view.FrPreview
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.fragments.FrFragment
import com.hms.referenceapp.huaweilens.main.interfaces.FrInterface
import com.hms.referenceapp.huaweilens.main.widgets.CameraXHelper
import kotlinx.android.synthetic.main.fr_fragment.*
import java.io.ByteArrayOutputStream

class FrPresenter(var view: FrFragment) : FrInterface.CameraPresenter,
    LifecycleOwner {

    private lateinit var customCamera: CameraXHelper
    var sensorManager: SensorManager? = null
    var sensor: Sensor? = null
    var sensorEventListener: SensorEventListener? = null


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
        customCamera = CameraXHelper(view, view.fr_camera_vieww)
        view.lifecycle.addObserver(customCamera)
    }

    override fun initButtonCallbacks() {

        view.fr_capture_imagee.setOnClickListener {

            view.showProgressDialog()

            customCamera.takePhoto(object : CameraXHelper.OnImageCapture {
                override fun onCaptureSuccess(bitmap: Bitmap) {
                    MenuActivity.SendImage.setBitmap(rotateBitmap(bitmap,false))
                    val intent = Intent(view.getViewActivity(), FrPreview::class.java)
                    view.getViewActivity()?.startActivity(intent)
                    view.dismissProgressDialog()
                    unRegisterSensor()
                }

                override fun onError(error: ImageCaptureException) {
                    Toast.makeText(view.requireContext(), "Capture failed.", Toast.LENGTH_LONG)
                        .show()
                    view.dismissProgressDialog()
                }
            })
        }



        view.fr_open_file_managerr.setOnClickListener {
            val intent = Intent(view.getViewActivity(), FileActivity::class.java)
            view.getViewActivity()?.startActivity(intent)

        }



        view.fr_add_imagee.setOnClickListener {
             view.showGallery()
        }

        view.fr_linearLayout3.setOnClickListener {
            customCamera.enableTorch()
            if (customCamera.getIsTorchEnabled()) {
                it.alpha = 1f
                view.fr_camera_torchh.setImageDrawable(
                    ContextCompat.getDrawable(view.requireContext(), R.drawable.ic_lightbulb_on)
                )
                view.fr_camera_torch.text = "Touch here to turn light off"
            } else {
                it.alpha = .5f
                view.fr_camera_torchh.setImageDrawable(
                    ContextCompat.getDrawable(view.requireContext(), R.drawable.ic_lightbulb)
                )
                view.fr_camera_torch.text = "Touch here to turn light on"
            }
        }




    }

    override fun initSensor() {
        var ctrl=true

        sensorManager = view.getViewActivity()?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {

                if (event.values[0]>=7&&ctrl){
                    view.dismissAnim()
                    ctrl=false
                }
                else if(!ctrl&& event.values[0]<7){
                    view.showAnim()
                    ctrl=true
                }

            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                Log.d("2341","AccuracyChange")
            }
        }

    }


    override fun registerSensor() {
        sensorManager!!.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun unRegisterSensor() {
        sensorManager!!.unregisterListener(sensorEventListener)
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
        return Uri.parse(path)
    }

    override fun getLifecycle(): Lifecycle {
        return view.lifecycle
    }

    fun rotateBitmap(bitmap: Bitmap,ctrl:Boolean): Bitmap{
        val matrix = Matrix()

        if (ctrl){
            matrix.postRotate(90F)
        }else{
            matrix.postRotate(270F)
        }


        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

        val rotatedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )

       return rotatedBitmap
    }


}