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
package com.hms.referenceapp.huaweilens.trt.processor

import android.graphics.*
import android.os.Build
import android.util.Log
import androidx.core.graphics.*
import androidx.palette.graphics.Palette
import com.hms.referenceapp.huaweilens.trt.activity.RemoteDetectionActivity
import com.hms.referenceapp.huaweilens.trt.helper.DetectAndTranslateHelper
import com.hms.referenceapp.huaweilens.trt.transactor.RemoteTextTransactor
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay
import com.huawei.hms.mlsdk.text.MLText
import kotlin.math.abs

class CloudDataProcessor {
    private var mGraphicOverlay: GraphicOverlay? = null
    private var mHmsMLVisionText: MLText? = null
    private var backGroundColor: Int? = null
    private var textColor:Int?=null


    fun setGraphicOverlay(graphicOverlay: GraphicOverlay?) {
        mGraphicOverlay = graphicOverlay
    }

    fun setText(text: MLText) {
        mHmsMLVisionText = text
    }

    fun drawView(canvas: Canvas,bitmap: Bitmap) {
        val rectPaint = Paint()
        rectPaint.style = Paint.Style.FILL
        val textPaint = Paint()
        textPaint.isAntiAlias=false
        textPaint.letterSpacing=0f
        textPaint.isLinearText=true
        textPaint.textAlign = Paint.Align.CENTER;
        rectPaint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        drawRectAndText(canvas, rectPaint, textPaint,bitmap)
    }

    private fun setColor(bitmap: Bitmap, rect: Rect, mlTextLine: MLText.TextLine?) {
        rect.checkBorders { tmp ->
            tmp?.let { rect ->
                if(rect.component1() + rect.width() <= bitmap.width
                    && rect.component2() + rect.height() <= bitmap.height) {
                    val croppedBitmap =
                        Bitmap.createBitmap(
                            bitmap,
                            rect.left,
                            rect.top,
                            rect.width(),
                            rect.height()
                        )


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val p = Palette.generate(croppedBitmap)
                        val myFirstX=mlTextLine?.vertexes?.get(0)?.x
                        val myFirstY=mlTextLine?.vertexes?.get(0)?.y
                        val touchedRGB: Color = bitmap.getColor(myFirstX!!, myFirstY!!)
                        backGroundColor =touchedRGB.toArgb()
                        textColor=getTextColor(p)
                    }
                }
                else{
                    backGroundColor=Color.WHITE
                    textColor=Color.BLACK
                }
            }
        }
    }

    private fun getTextColor(p: Palette): Int? {
        return if(isColorDark(backGroundColor!!)){
            p.getLightMutedColor(0)
        }else{
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

    private fun drawRectAndText(
        canvas: Canvas,
        rectPaint: Paint,
        textPaint: Paint,
        bitmap: Bitmap

    ) {
        for (i in mHmsMLVisionText?.blocks?.indices!!) {
            val rect = RectF(mHmsMLVisionText!!.blocks[i].contents[0].border)
            rect.left = translateX(rect.left, mGraphicOverlay)
            rect.top = translateY(rect.top, mGraphicOverlay)
            rect.right = translateX(rect.right, mGraphicOverlay)
            rect.bottom = translateY(rect.bottom, mGraphicOverlay)
            setColor(bitmap,rect.toRect(),mHmsMLVisionText!!.blocks[i].contents[0])
            rectPaint.color = backGroundColor?:Color.GRAY
            textPaint.color = textColor?:Color.WHITE
            setTextSizeForWidthandHeight(
                rect.height(),
                textPaint,
                rect.width(),
                mHmsMLVisionText!!.blocks[i].contents[0].stringValue
            )
            textPaint.letterSpacing = 0f
            canvas.drawRect(rect, rectPaint)
            if (RemoteDetectionActivity.SendRemoteLangCode.getLangCode() == "none") {
                setTextSizeForWidthandHeight(
                    rect.height(),
                    textPaint,
                    rect.width(),
                    mHmsMLVisionText!!.blocks[i].contents[0].stringValue
                )
                drawCenterText(mHmsMLVisionText!!.blocks[i].contents[0].stringValue, rect, canvas, textPaint)
            } else {
                val detectedLanguage =
                    DetectAndTranslateHelper.detectAndTranslateLanguageReturn(mHmsMLVisionText!!.stringValue)
                val translated = RemoteDetectionActivity.SendRemoteLangCode.langcode?.let {
                    DetectAndTranslateHelper.translateTextRemoteWithReturn(
                        mHmsMLVisionText!!.blocks[i].contents[0].stringValue,
                        detectedLanguage,
                        it
                    )
                }
                if (translated != null) {
                    setTextSizeForWidthandHeight(
                        rect.height(),
                        textPaint,
                        rect.width(),
                        translated
                    )
                    drawCenterText(translated, rect, canvas, textPaint)
                } else {
                    setTextSizeForWidthandHeight(
                        rect.height(),
                        textPaint,
                        rect.width(),
                        mHmsMLVisionText!!.blocks[i].contents[0].stringValue
                    )
                    drawCenterText(mHmsMLVisionText!!.blocks[i].contents[0].stringValue, rect, canvas, textPaint)
                }
            }
        }


    }

    private fun drawCenterText(
        text: String?,
        rectF: RectF,
        canvas: Canvas,
        paint: Paint
    ) {
        val align: Paint.Align = paint.textAlign
        val x: Float
        val y: Float
        x = when {
            align === Paint.Align.LEFT -> {
                rectF.centerX() - paint.measureText(text) / 2
            }
            align === Paint.Align.CENTER -> {
                rectF.centerX()
            }
            else -> {
                rectF.centerX() + paint.measureText(text) / 2
            }
        }
        val metrics = paint.fontMetrics
        val acent: Float = abs(metrics.ascent)
        val descent: Float = abs(metrics.descent)
        y = rectF.centerY() + (acent - descent) / 2f
        canvas.drawText(text!!, x, y, paint)
        Log.e(
            "ghui", "top:" + metrics.top.toString() + ",ascent:" + metrics.ascent
                .toString() + ",dscent:" + metrics.descent.toString() + ",leading:" + metrics.leading.toString() + ",bottom" + metrics.bottom
        )
    }

    private fun setTextSizeForWidthandHeight(
        size:Float,paint: Paint, desiredWidth: Float,
        text: String
    ) {

        paint.textSize = size
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)


        val desiredTextSize = size * desiredWidth / bounds.width()

        paint.textSize = desiredTextSize
    }

    private fun scaleX(x: Float, graphicOverlay: GraphicOverlay?): Float {
        return x * graphicOverlay!!.widthScaleFactor
    }

    private fun scaleY(y: Float, graphicOverlay: GraphicOverlay?): Float {
        return y * graphicOverlay!!.heightScaleFactor
    }

    private fun translateX(x: Float, graphicOverlay: GraphicOverlay?): Float {
        return scaleX(x, graphicOverlay)
    }

    private fun translateY(y: Float, graphicOverlay: GraphicOverlay?): Float {
        return scaleY(y, graphicOverlay)
    }

}