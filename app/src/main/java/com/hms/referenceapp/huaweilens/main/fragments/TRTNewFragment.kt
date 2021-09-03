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

package com.hms.referenceapp.huaweilens.main.fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.interfaces.TrtFragmentInterface
import com.hms.referenceapp.huaweilens.main.presenters.TrtFragmentPresenter
import com.hms.referenceapp.huaweilens.odt.activities.ClassificationActivity
import com.hms.referenceapp.huaweilens.trt.activity.RemoteDetectionActivity
import com.hms.referenceapp.huaweilens.trt.camera.CameraSource
import com.hms.referenceapp.huaweilens.trt.camera.CameraSourcePreview
import com.hms.referenceapp.huaweilens.trt.processor.TextRecognitionProcessor
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel
import kotlinx.android.synthetic.main.fragment_trt.*
import kotlinx.android.synthetic.main.fragment_trt.view.*
import java.io.IOException
import java.util.concurrent.ThreadLocalRandom
import kotlin.concurrent.thread


class TRTNewFragment : Fragment(),TrtFragmentInterface.CameraView {
    private lateinit var presenter: TrtFragmentInterface.CameraPresenter

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trt, container, false)
        MLApplication.getInstance().apiKey =
            "CgB6e3x9ztsd3VJtjuiA1Y3KKZvhFop97qcIeK6SieCUfTwji9HANSwK/mXeIyLncVCRn7IBwrc9HWttO5iGMdl9"
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        presenter = TrtFragmentPresenter(this)
        preview=view.camera_source_preview as CameraSourcePreview
        presenter.initPreview(preview)
        if (preview == null) {
            Log.d("TAG", "Preview is null")
        }
        graphicOverlay=view.graphics_overlay as GraphicOverlay
        presenter.initOverlay(graphicOverlay)
        if (graphicOverlay == null) {
            Log.d("TAG", "graphicOverlay is null")
        }


        presenter.initSpinner()
        createCameraSource()
        presenter.initButtonCallbacks(cameraSource!!)

    }

    override fun getViewActivity(): FragmentActivity? {
        return  activity
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        preview!!.stop()
    }

    override  fun createCameraSource() {
        if (cameraSource == null) {
            cameraSource = CameraSource(requireActivity(), graphicOverlay!!)
            cameraSource!!.setFacing(CameraSource.CAMERA_FACING_BACK)
        }
            cameraSource!!.setMachineLearningFrameProcessor(TextRecognitionProcessor())


    }

    override fun startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d("TAG", "resume: Preview is null")
                }
                if (graphicOverlay == null) {
                    Log.d("TAG", "resume: graphOverlay is null")
                }
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e("TAG", "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onResume")
        startCameraSource()
    }

    /**
     * Stops the camera.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onPause() {
        super.onPause()
        preview!!.stop()
        takePicture.setImageBitmap(null)
        ln_flash.isClickable=true
        text_imageSwitch.isClickable=true
        flash_text?.text = requireContext().resources.getString(R.string.touch_here_to_turn_light_on)
        flash?.setImageDrawable(requireContext().getDrawable(R.drawable.ic_lightbulb))
        ln_flash.alpha = 0.5f
        presenter.closeSpinner()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource!!.release()
            createCameraSource()
            startCameraSource()
        }
    }

    override fun onStart() {
        super.onStart()
        preview!!.stop()
    }

}