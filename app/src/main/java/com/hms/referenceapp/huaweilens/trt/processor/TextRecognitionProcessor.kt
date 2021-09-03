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
package com.hms.referenceapp.huaweilens.trt.processor

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.Log
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.graphics.component3
import androidx.core.graphics.component4
import androidx.palette.graphics.Palette
import com.hms.referenceapp.huaweilens.trt.helper.DetectAndTranslateHelper
import com.hms.referenceapp.huaweilens.trt.views.FrameMetadata
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay.Graphic
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.common.MLFrame.Property.IMAGE_FORMAT_NV21
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean


class TextRecognitionProcessor() {
    private var isFirst = true

    private val setting: MLLocalTextSetting = MLLocalTextSetting.Factory()
        .setOCRMode(MLLocalTextSetting.OCR_TRACKING_MODE)
        .create()
    private val detectorHuawei: MLTextAnalyzer =
        MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting)

    private val shouldThrottle = AtomicBoolean(false)

    fun stop() {
        try {
            detectorHuawei.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: $e")
        }
    }

    @Throws(MLException::class)
    fun process(data: ByteBuffer?, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay) {
        if (shouldThrottle.get()) {
            return
        }
        val metadata =
            MLFrame.Property.Creator().setFormatType(IMAGE_FORMAT_NV21)
                .setWidth(frameMetadata.width).setHeight(frameMetadata.height)
                .setQuadrant(frameMetadata.rotation).create()
        detectInVisionImage(MLFrame.fromByteBuffer(data, metadata), graphicOverlay)


    }

    private fun setColor(bitmap: Bitmap, rect: Rect, mlTextLine: MLText.TextLine?) {
        rect.checkBorders { tmp ->
            tmp?.let { rect ->
                if (rect.component1() + rect.width() <= bitmap.width
                    && rect.component2() + rect.height() <= bitmap.height
                ) {
                    val croppedBitmap =
                        Bitmap.createBitmap(
                            bitmap,
                            rect.left,
                            rect.top,
                            rect.width(),
                            rect.height()
                        )

                    val p = Palette.generate(croppedBitmap)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val myFirstX = mlTextLine?.vertexes?.get(0)?.x
                        val myFirstY = mlTextLine?.vertexes?.get(0)?.y
                        val touchedRGB: Color = bitmap.getColor(myFirstX!!, myFirstY!!)
                        val backColor = touchedRGB.toArgb()
                        SendImageColor.setBackGroundColor(backColor)
                        getTextColor(p)?.let { SendImageColor.setTextColor(it) }
                    }
                } else {
                    SendImageColor.setBackGroundColor(Color.WHITE)
                    SendImageColor.setTextColor(Color.BLACK)
                }
            }
        }
    }

    private fun getTextColor(p: Palette): Int? {
        return if (SendImageColor.getBackGroundColor()?.let { isColorDark(it) }!!) {
            p.getLightMutedColor(0)
        } else {
            p.getDarkMutedColor(0)
        }

    }

    private fun isColorDark(color: Int): Boolean {
        val darkness =
            1 - (0.2126 * Color.red(color) + 0.7152 * Color.green(
                color
            ) + 0.0722 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    private inline fun <R> Rect.checkBorders(block: (Rect?) -> R): R? =
        if (this.component1() > 0 && this.component2() > 0 && this.component3() > 0 && this.component4() > 0 && this.width() > 0 && this.height() > 0)
            block(this)
        else
            block(null)

    private fun detectInImage(image: MLFrame?): Task<MLText> {
        return detectorHuawei.asyncAnalyseFrame(image)
    }

    private fun onSuccess(
        image: MLFrame,
        results: MLText,
        graphicOverlay: GraphicOverlay
    ) {
        val blocks = results.blocks
        if (size == 0) {
            constantMLTextBlock = results.blocks
            size = constantMLTextBlock!!.size
            graphicOverlay.clear()
            size = constantMLTextBlock!!.size
            for (i in constantMLTextBlock!!.indices) {
                val lines = constantMLTextBlock!![i].contents
                val myBorder = constantMLTextBlock!![i].border
                for (j in lines.indices) {
                    if (lines[j].stringValue.length > 1) {
                        setColor(image.previewBitmap, myBorder, lines[j])
                        val textGraphic: Graphic = TextGraphic(graphicOverlay, lines[j])
                        graphicOverlay.add(textGraphic)
                    }
                }
            }
            myBitmap = image.previewBitmap
        }
        if (size != 0 && size != blocks.size && myBitmap != image.previewBitmap && results.blocks != constantMLTextBlock) {
            graphicOverlay.clear()
            constantMLTextBlock = results.blocks
            size = constantMLTextBlock!!.size
            for (i in constantMLTextBlock!!.indices) {
                val lines = constantMLTextBlock!![i].contents
                val myBorder = constantMLTextBlock!![i].border
                for (j in lines.indices) {
                    if (lines[j].stringValue.length > 1) {
                        setColor(image.previewBitmap, myBorder, lines[j])
                        val textGraphic: Graphic = TextGraphic(graphicOverlay, lines[j])
                        graphicOverlay.add(textGraphic)
                    }
                }
            }

        }
    }

    private fun onFailure(e: Exception) {
        Log.w(TAG, "Text detection failed.$e")
    }

    private fun detectInVisionImage(
        image: MLFrame,
        graphicOverlay: GraphicOverlay
    ) {
        detectInImage(image)
            .addOnSuccessListener { results ->
                shouldThrottle.set(false)
                if (isFirst && size == 0 && constantMLTextBlock == null) {
                    val detectedLanguage =
                        DetectAndTranslateHelper.detectAndTranslateLanguageReturn(
                            results.stringValue.replace(
                                "\n",
                                " ",
                                false
                            )
                        )
                    SendSourceCode.setSourceCode(detectedLanguage)
                    this@TextRecognitionProcessor.onSuccess(image, results, graphicOverlay)
                    isFirst = false

                } else if (!isFirst && size != results.blocks.size && myBitmap != image.previewBitmap && constantMLTextBlock != results.blocks) {
                    val detectedLanguage =
                        DetectAndTranslateHelper.detectAndTranslateLanguageReturn(
                            results.stringValue.replace(
                                "\n",
                                " ",
                                false
                            )
                        )
                    SendSourceCode.setSourceCode(detectedLanguage)
                    this@TextRecognitionProcessor.onSuccess(image, results, graphicOverlay)

                }
            }
            .addOnFailureListener { e ->
                shouldThrottle.set(false)
                this@TextRecognitionProcessor.onFailure(e)
            }

        shouldThrottle.set(true)
    }

    companion object {
        private const val TAG = "TextRecProc"
        private var size: Int = 0
        private var myBitmap: Bitmap? = null
        private var constantMLTextBlock: List<MLText.Block>? = null
    }

    class SendImageColor {
        companion object {
            private var backGroundColor: Int? = null
            private var textColor: Int? = null

            fun getBackGroundColor(): Int? {
                return backGroundColor
            }

            fun setBackGroundColor(backColor: Int) {
                this.backGroundColor = backColor
            }

            fun getTextColor(): Int? {
                return textColor
            }

            fun setTextColor(textColor: Int) {
                this.textColor = textColor
            }

        }
    }

    class SendSourceCode {
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