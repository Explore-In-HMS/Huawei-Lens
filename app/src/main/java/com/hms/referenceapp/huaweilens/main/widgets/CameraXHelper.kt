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

package com.hms.referenceapp.huaweilens.main.widgets

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.util.DisplayMetrics
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraXHelper constructor(
    private val fragment: Fragment,
    private val cameraView: PreviewView
) : LifecycleObserver {

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    var flashMode: Int = ImageCapture.FLASH_MODE_AUTO

    init {
        initCamera()
    }

    fun setAlpha(alpha:Float)
    {
        cameraView.alpha = alpha
    }

    private fun initCamera() {
        cameraView.post {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(fragment.requireContext())
            cameraProviderFuture.addListener(Runnable {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(fragment.requireContext()))
        }
    }

    private fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also { cameraView.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = cameraView.display.rotation
        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .setFlashMode(flashMode)
            .build()
        cameraProvider?.unbindAll()

        try {
            camera = cameraProvider?.bindToLifecycle(
                fragment.viewLifecycleOwner, cameraSelector, preview, imageCapture
            )
            preview?.setSurfaceProvider(cameraExecutor,cameraView.surfaceProvider)
        } catch (exc: Exception) {
            Log.e("Camera Presenter", "Use case binding failed", exc)
        }
    }


    /**
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /**
     * @param image image file that will be converted to bitmap
     * @return bitmap
     */
    private fun imageToBitmap(image: Image): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
    }

    /**
     * set flash mode as ON,OFF,AUTO.
     * @param mode
     * @see FlashModes
     */
    fun setFlashMode(mode: FlashModes) {
        flashMode = when(mode){
            FlashModes.ON -> ImageCapture.FLASH_MODE_ON
            FlashModes.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashModes.OFF -> ImageCapture.FLASH_MODE_OFF
        }
        bindCameraUseCases()
    }



    /**
     * @param onImageCapture returns bitmap or an error
     *
     * @see OnImageCapture.onCaptureSuccess returns bitmap when image capture is successfully
     * @see OnImageCapture.onError returns error when image capture error occurs
     */
    fun takePhoto(onImageCapture: OnImageCapture) {
        imageCapture?.takePicture(cameraExecutor, object :
            ImageCapture.OnImageCapturedCallback() {
            @SuppressLint("UnsafeExperimentalUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
                image.image?.let {
                    onImageCapture.onCaptureSuccess(imageToBitmap(it))
                }
                super.onCaptureSuccess(image)
            }

            override fun onError(exception: ImageCaptureException) {
                onImageCapture.onError(exception)
                super.onError(exception)
            }
        })
    }

    fun getIsTorchEnabled(): Boolean {
        return camera?.cameraInfo?.torchState?.value == TorchState.ON
    }
    /**
     * @param isEnabled true is activate to enable torch.
     */
    fun enableTorch() {
        camera?.cameraControl?.enableTorch(camera?.cameraInfo?.torchState?.value == TorchState.OFF)
    }

    /**
     * @return if device supports Lens Facing, returns true
     */
    fun supportSwitchLensFacing(): Boolean = hasBackCamera() && hasFrontCamera()

    /**
     * Switch between front and back lenses
     */
    fun switchLensFacing() {
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        bindCameraUseCases()
    }

    /**
     * Fragment changes causes screen freezes, need to call camera again
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        initCamera()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroyView(){
        cameraExecutor.shutdown()
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause(){
        camera = null
    }

    /** Returns true if the device has an available back camera. False otherwise */
    fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    interface OnImageCapture {
        fun onCaptureSuccess(bitmap: Bitmap)
        fun onError(error: ImageCaptureException)
    }

    enum class FlashModes{
        ON,OFF,AUTO
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}