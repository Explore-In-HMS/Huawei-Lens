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

/**
 * Copyright 2020. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hms.referenceapp.huaweilens.trt.transactor

import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import com.hms.referenceapp.huaweilens.trt.callback.CouldInfoResultCallBack
import com.hms.referenceapp.huaweilens.trt.camera.FrameMetadata
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.trt.camera.CameraSource
import com.hms.referenceapp.huaweilens.trt.helper.DetectAndTranslateHelper
import com.hms.referenceapp.huaweilens.trt.processor.TextRecognitionProcessor
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.text.MLRemoteTextSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer

open class RemoteTextTransactor(handler: Handler) {

    private val detector: MLTextAnalyzer
    private var callBack: CouldInfoResultCallBack? = null
    private val handler: Handler

    private fun detectInImage(image: MLFrame?): Task<MLText> {
        return detector.asyncAnalyseFrame(image)
    }

    fun getCallBack(callBack: CouldInfoResultCallBack) {
        this.callBack = callBack
    }

    private fun detectInVisionImage(
        bitmap: Bitmap?, image: MLFrame, metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay?
    ) {
        detectInImage(image)
            .addOnSuccessListener { results ->
                if (metadata == null || metadata.cameraFacing == CameraSource.CAMERA_FACING_BACK) {
                    val detectedLanguage = DetectAndTranslateHelper.detectAndTranslateLanguageReturn(results.stringValue.replace("\n"," ",false))
                    SendRemoteSourceCode.setSourceCode(detectedLanguage)
                    this.onSuccess(bitmap, results, graphicOverlay)
                }
            }
            .addOnFailureListener { e -> this.onFailure(e) }
    }


    fun process(bitmap: Bitmap?, graphicOverlay: GraphicOverlay?) {
        val frame = MLFrame.Creator().setBitmap(bitmap).create()
        detectInVisionImage(bitmap, frame, null, graphicOverlay)
    }

    private fun onSuccess(
        originalCameraImage: Bitmap?,
        results: MLText,
        graphicOverlay: GraphicOverlay?
    ) {
        handler.sendEmptyMessage(Constants.GET_DATA_SUCCESS)
        graphicOverlay?.clear()
        callBack!!.onSuccessForText(originalCameraImage, results, graphicOverlay)
    }

    private fun onFailure(e: Exception?) {
        handler.sendEmptyMessage(Constants.GET_DATA_FAILED)
        if (e != null) {
            Log.e(TAG, "Remote text detection failed: " + e.message)
        }
    }

    companion object {
        private const val TAG = "RemoteTextTransactor"
    }

    init {
        val options = MLRemoteTextSetting.Factory().setBorderType(MLRemoteTextSetting.ARC).create()
        detector = MLAnalyzerFactory.getInstance().getRemoteTextAnalyzer(options)
        this.handler = handler
    }

    class SendRemoteSourceCode {
        companion object {
            var langcode: String? = null
            fun getSourceCode(): String? {
                return langcode
            }

            fun setSourceCode(langcode: String) {
                this.langcode = langcode
            }
        }
    }

}