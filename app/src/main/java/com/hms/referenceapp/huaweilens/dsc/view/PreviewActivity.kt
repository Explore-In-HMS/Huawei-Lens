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

package com.hms.referenceapp.huaweilens.dsc.view

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.dsc.cropper.CropImage
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.dsc.*
import com.huawei.hms.mlsdk.text.MLRemoteTextSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import java.io.ByteArrayOutputStream
import kotlin.properties.Delegates


class PreviewActivity : AppCompatActivity() {

    lateinit var check: ImageView
    lateinit var ignore: ImageView
    lateinit var photo: ImageView
    lateinit var edit: ImageView
    lateinit var progressbar: ProgressDialog
    val setting = MLDocumentSkewCorrectionAnalyzerSetting.Factory().create()
    val analyzer: MLDocumentSkewCorrectionAnalyzer =
        MLDocumentSkewCorrectionAnalyzerFactory.getInstance()
            .getDocumentSkewCorrectionAnalyzer(setting)
    val settingT: MLRemoteTextSetting = MLRemoteTextSetting.Factory()
        .setTextDensityScene(MLRemoteTextSetting.OCR_LOOSE_SCENE)
        .create()
    val analyzerT: MLTextAnalyzer =
        MLAnalyzerFactory.getInstance().getRemoteTextAnalyzer(settingT)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_preview)
        check = findViewById(R.id.check)
        ignore = findViewById(R.id.ignorer)
        photo = findViewById(R.id.imagepreview)
        edit = findViewById(R.id.dsc_editer)
        progressbar = ProgressDialog(this)
        progressbar.setCanceledOnTouchOutside(false)
        progressbar.setMessage("Results are loading, please wait")
        bitmap= MenuActivity.SendImage.getBitmap()!!
        uri = getImageUri(bitmap,Bitmap.CompressFormat.JPEG,100)!!
        photo.setImageBitmap(bitmap)


        check.setOnClickListener {

            progressbar.show()
            initDocumentSkewCorrectionService(bitmap)
        }

        edit.setOnClickListener {

            CropImage.activity(uri)
                .start(this)

        }

        ignore.setOnClickListener {

            onBackPressed()

        }
    }

    override fun onBackPressed() {
        progressbar.dismiss()
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {


            if (CropImage.getActivityResult(data) != null) {
                val result = CropImage.getActivityResult(data)
                val resultUri = result.uri

                bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    this.contentResolver?.let {
                        ImageDecoder.createSource(
                            it,
                            resultUri
                        )
                    }?.let {
                        ImageDecoder.decodeBitmap(
                            it
                        )
                    }!!
                } else {
                    MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        resultUri
                    )
                }

                photo.setImageBitmap(bitmap)

                resultUri.path?.let { garbage?.add(it) }
                uri = resultUri


            }

            if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Crop failed.", Toast.LENGTH_LONG).show()
            }
        }

    }

    fun initDocumentSkewCorrectionService(imageBit: Bitmap?) {


        var frame = MLFrame.fromBitmap(imageBit)
        var detectTask: Task<MLDocumentSkewDetectResult> = analyzer.asyncDocumentSkewDetect(frame)
        detectTask.addOnSuccessListener {

            if (it.leftTopPosition != null && it.leftBottomPosition != null
                && it.rightTopPosition != null && it.rightBottomPosition != null
            ) {

                var leftTop: Point = it.leftTopPosition
                var rightTop: Point = it.rightTopPosition
                var leftBottom: Point = it.leftBottomPosition
                var rightBottom: Point = it.rightBottomPosition

                var coordinates: MutableList<Point> = mutableListOf()

                coordinates.add(leftTop)
                coordinates.add(rightTop)
                coordinates.add(leftBottom)
                coordinates.add(rightBottom)

                var coordinateData = MLDocumentSkewCorrectionCoordinateInput(coordinates)

                var correctionTask: Task<MLDocumentSkewCorrectionResult> =
                    analyzer.asyncDocumentSkewCorrect(frame, coordinateData)
                correctionTask.addOnSuccessListener {


                    var urii: Uri? = getImageUri(it.corrected, Bitmap.CompressFormat.PNG, 100)
                    if (urii != null) {
                        urii.path?.let { it1 -> garbage?.add(it1) }
                    }


                    if (urii != null) {
                        SkewPreview.dsc_uri = urii
                    }
                    SkewPreview.dsc_status = true

                    textRecognizer(it.corrected)


                }.addOnFailureListener {
                    //correction fail
                    if (imageBit != null) {
                        textRecognizer(imageBit)
                        SkewPreview.dsc_status = false
                        SkewPreview.dsc_uri = uri

                    }
                }
            } else {
                //coordinate null
                if (imageBit != null) {
                    textRecognizer(imageBit)
                    SkewPreview.dsc_status = false
                    SkewPreview.dsc_uri = uri
                }
            }
        }.addOnFailureListener {

            //skew failed
            if (imageBit != null) {
                textRecognizer(imageBit)
                SkewPreview.dsc_status = false
                SkewPreview.dsc_uri = uri

            }
        }

        analyzer?.stop()
    }

    private fun textRecognizer(bitmap: Bitmap) {


        var frame = MLFrame.fromBitmap(bitmap)
        var task: Task<MLText> = analyzerT.asyncAnalyseFrame(frame)
        task.addOnSuccessListener { text ->


            progressbar.dismiss()
            PreviewActivity.text = text
            ocrContoller = true


            var intent = Intent(this, SkewPreview::class.java)
            startActivity(intent)
        }

            .addOnFailureListener {
                progressbar.dismiss()
                ocrContoller = false
                var intent = Intent(this, SkewPreview::class.java)
                startActivity(intent)

            }

    }

    fun getImageUri(src: Bitmap, format: Bitmap.CompressFormat?, quality: Int): Uri? {
        val os = ByteArrayOutputStream()
        src.compress(format, quality, os)
        val path: String = MediaStore.Images.Media.insertImage(
            this.contentResolver,
            src,
            "Huawei-Lens",
            null
        )
        return Uri.parse(path)
    }


    companion object {
        lateinit var text: MLText
        var garbage: MutableList<String>? = null
        lateinit var bitmap: Bitmap
        lateinit var uri:Uri
        var ocrContoller by Delegates.notNull<Boolean>()
    }


}