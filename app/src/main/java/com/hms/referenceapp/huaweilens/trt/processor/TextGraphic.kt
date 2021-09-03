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

import android.graphics.*
import android.util.Log
import com.hms.referenceapp.huaweilens.main.presenters.TrtFragmentPresenter.SendLangCode.Companion.langcode
import com.hms.referenceapp.huaweilens.trt.helper.DetectAndTranslateHelper
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay.Graphic
import com.huawei.hms.mlsdk.text.MLText
import kotlin.math.abs


/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class TextGraphic internal constructor(
    overlay: GraphicOverlay?,
    private val mlText: MLText.TextLine?
) : Graphic(overlay!!) {
    private val rectPaint: Paint = Paint()
    private val textPaint: Paint
    private var translated:String?=null


    /** Draws the text block annotations for position, size, and raw value on the supplied canvas.  */
    override fun draw(canvas: Canvas?) {
        checkNotNull(mlText) { "Attempting to draw a null text." }
        // Draws the bounding box around the TextBlock.
        val rect = RectF(mlText.border)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas!!.drawRect(rect, rectPaint)
        setTextSizeForWidthandHeight(rect.height()/2,textPaint,rect.width(),mlText.stringValue)
        textPaint.letterSpacing=0f
        val detectedLanguage = DetectAndTranslateHelper.detectAndTranslateLanguageReturn(mlText.stringValue)
        if (langcode == "none" && mlText.language!="" && detectedLanguage!="") {
            drawCenterText(mlText.stringValue,rect,canvas,textPaint)
        } else {
            translated = if(detectedLanguage!=""){
                langcode?.let {
                    DetectAndTranslateHelper.translateTextRemoteWithReturn(mlText.stringValue, detectedLanguage, it)
                }
            } else{
                null
            }


            if (translated != null) {
                setTextSizeForWidthandHeight(rect.height(),textPaint,rect.width(),translated!!)
                drawCenterText(translated,rect,canvas,textPaint)
            }
            else {
                drawCenterText(mlText.stringValue,rect,canvas,textPaint)
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

    companion object {
        private const val STROKE_WIDTH = 4.0f
    }

    init {
        rectPaint.style = Paint.Style.FILL
        rectPaint.strokeWidth = STROKE_WIDTH
        rectPaint.color =  TextRecognitionProcessor.SendImageColor.getBackGroundColor()?:Color.WHITE
        rectPaint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        textPaint = Paint()
        textPaint.isAntiAlias=false
        textPaint.letterSpacing=0f
        textPaint.color = TextRecognitionProcessor.SendImageColor.getTextColor()?:Color.BLACK
        textPaint.isLinearText=true
        textPaint.textAlign = Paint.Align.CENTER;
        postInvalidate()

    }

}