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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("DEPRECATION")

package com.hms.referenceapp.huaweilens.trt.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.trt.callback.CouldInfoResultCallBack
import com.hms.referenceapp.huaweilens.trt.processor.CloudDataProcessor
import com.hms.referenceapp.huaweilens.trt.transactor.RemoteTextTransactor
import com.hms.referenceapp.huaweilens.trt.util.BitmapUtils.loadFromPath
import com.hms.referenceapp.huaweilens.trt.util.BitmapUtils.recycleBitmap
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.common.language.LanguageActivity
import com.hms.referenceapp.huaweilens.common.translate.GetLanguageArray
import com.hms.referenceapp.huaweilens.trt.processor.TextRecognitionProcessor
import com.hms.referenceapp.huaweilens.trt.views.overlay.ZoomImageView
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel
import kotlinx.android.synthetic.main.activity_remote_detection.*
import java.lang.ref.WeakReference

class RemoteDetectionActivity : AppCompatActivity(),
    View.OnClickListener {
    private var preview: ImageView? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var changeImageView: ZoomImageView? = null
    private var imageUri: Uri? = null
    private var maxWidthOfImage: Int? = null
    private var maxHeightOfImage: Int? = null
    private var imageTransactor: RemoteTextTransactor? = null
    private var bitmapCopy: Bitmap? = null
    private var imageBitmap: Bitmap? = null
    private var progressDialog: Dialog? = null
    private var cloudDataProcessor: CloudDataProcessor? = null
    private val textResultCallBack: CouldInfoResultCallBack = MyCouldInfoResultCallBack()
    private val mHandler: Handler? = MsgHandler(this)
    var translateList: Array<String?>? = null
    private var myTranslateList: MutableList<String?>? = null
    private var mySourceList: MutableList<String?>? = null
    private var leftText: String = ""
    private var rightText: String = ""
    private var translateLangCode: String = ""
    private var sourceLangCode: String = ""
    val manager = MLLocalModelManager.getInstance()
    private var mLanguageArray: GetLanguageArray? = null

    private class MsgHandler(mainActivity: RemoteDetectionActivity) : Handler() {
        var mMainActivityWeakReference: WeakReference<RemoteDetectionActivity> =
            WeakReference(mainActivity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val mainActivity = mMainActivityWeakReference.get() ?: return
            Log.d(TAG, "msg what :" + msg.what)
            if (msg.what == Constants.GET_DATA_SUCCESS) {
                mainActivity.handleGetDataSuccess()
            } else if (msg.what == Constants.GET_DATA_FAILED) {
                mainActivity.handleGetDataFailed()
            }
        }

    }

    private fun handleGetDataSuccess() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
        mHandler!!.removeCallbacks(myRunnable)
    }

    private fun handleGetDataFailed() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
        mHandler!!.removeCallbacks(myRunnable)
        Toast.makeText(this, this.getString(R.string.get_data_failed), Toast.LENGTH_SHORT).show()
    }

    private val myRunnable = Runnable {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
        Toast.makeText(
            this@RemoteDetectionActivity.applicationContext,
            this@RemoteDetectionActivity.getString(R.string.get_data_failed),
            Toast.LENGTH_SHORT
        ).show()
    }
    private val detectRunnable = Runnable { loadImageAndSetTransactor() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = this.intent
        var type: String? = null
        try {
            type = intent.getStringExtra(Constants.ADD_PICTURE_TYPE)
        } catch (e: RuntimeException) {
            Log.e(
                TAG,
                "Get intent value failed: " + e.message
            )
        }

        this.setContentView(R.layout.activity_remote_detection)
        initSpinner()
        findViewById<View>(R.id.back).setOnClickListener(this)
        preview = findViewById(R.id.still_preview)
        graphicOverlay = findViewById(R.id.still_overlay)
        changeImageView = findViewById(R.id.changeOverlay)
        findViewById<View>(R.id.getImageButton)?.setOnClickListener(this)
        createImageTransactor()
        cloudDataProcessor = CloudDataProcessor()
        when (type) {
            Constants.TYPE_SELECT_IMAGE -> {
                selectLocalImage()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initSpinner() {

        mLanguageArray = GetLanguageArray(GetLanguageArray.LOCAL, this, false)

        translateList = mLanguageArray!!.getNames()
        myTranslateList = translateList!!.toMutableList()
        myTranslateList!!.add(0, "Select language")
        SendRemoteLangCode.setLangCode("none")
        scroll_choice_right.addItems(
            myTranslateList,
            myTranslateList!!.indexOf(txt_language_out_right.text)
        )
        scroll_choice_right.setOnItemSelectedListener { scrollChoice, position, name ->
            txt_language_out_right.text = name
            translateLangCode = ""
            if (position != 0) {
                translateLangCode = mLanguageArray!!.getLanguages()[position - 1].iso6391
            }

        }

        val sourceList = mLanguageArray!!.getNames()
        mySourceList = sourceList.toMutableList()
        mySourceList!!.add(0, "(Auto-detect)")
        TextRecognitionProcessor.SendSourceCode.setSourceCode("none")
        scroll_choice_left.addItems(
            mySourceList,
            mySourceList!!.indexOf(txt_language_out_right.text)
        )
        scroll_choice_left.setOnItemSelectedListener { scrollChoice, position, name ->
            txt_language_out_left.text = name
            sourceLangCode = ""
            if (position != 0) {
                sourceLangCode = mLanguageArray!!.getLanguages()[position - 1].iso6391
            }
        }

        ln_collapse.setOnClickListener {
            ln_expand.visibility = View.VISIBLE
            ln_collapse.background = null
            leftText = txt_language_out_left.text.toString()
            rightText = txt_language_out_right.text.toString()
            scroll_choice_right.addItems(
                myTranslateList,
                myTranslateList!!.indexOf(txt_language_out_right.text)
            )
            scroll_choice_left.addItems(
                mySourceList,
                mySourceList!!.indexOf(txt_language_out_left.text)
            )
        }

        img_close.setOnClickListener {
            ln_collapse.visibility = View.VISIBLE
            ln_collapse.background = this.getDrawable(R.drawable.transcription_type_radiogroup_trt)
            ln_expand.visibility = View.GONE
            txt_language_out_left.text = leftText
            txt_language_out_right.text = rightText
        }
        img_check.setOnClickListener {
            ln_collapse.visibility = View.VISIBLE
            ln_collapse.background = getDrawable(R.drawable.transcription_type_radiogroup_trt)
            ln_expand.visibility = View.GONE
            val sourceCode = RemoteTextTransactor.SendRemoteSourceCode.getSourceCode()
            if (sourceLangCode == "") {
                sourceLangCode = sourceCode!!
            }

            if (translateLangCode != "" && translateLangCode != "en" && sourceLangCode != "en") {
                manager.isModelExist(
                    MLLocalTranslatorModel.Factory(
                        translateLangCode
                    ).create()
                ).addOnSuccessListener { it1 ->
                    if (it1) {
                        SendRemoteLangCode.setLangCode(translateLangCode)
                        manager.isModelExist(
                            MLLocalTranslatorModel.Factory(sourceLangCode).create()
                        ).addOnSuccessListener {
                            if (!it) {
                                onSituationSuccess(it, "sourceLanguageCode", sourceLangCode)
                            } else {
                                reloadAndDetectImage()
                            }
                        }

                    } else {
                        manager.isModelExist(
                            MLLocalTranslatorModel.Factory(sourceLangCode).create()
                        ).addOnSuccessListener {
                            val intent = Intent(this, LanguageActivity::class.java)
                            SendRemoteLangCode.setLangCode(translateLangCode)
                            if (!it) {
                                intent.putExtra("sourceLanguageCode", sourceLangCode)
                            }
                            intent.putExtra("LanguageCode", translateLangCode)
                            startActivity(intent)
                        }
                    }
                }
            } else if (translateLangCode == "en" && sourceLangCode != "en") {
                SendRemoteLangCode.setLangCode(translateLangCode)
                manager.isModelExist(
                    MLLocalTranslatorModel.Factory(
                        sourceLangCode
                    ).create()
                ).addOnSuccessListener { it ->
                    if (it) {
                        SendRemoteLangCode.setLangCode(translateLangCode)
                        reloadAndDetectImage()
                    } else {
                        onSituationSuccess(it, "sourceLanguageCode", sourceLangCode)
                    }
                }
            } else if (sourceLangCode == "en" && translateLangCode != "en") {
                manager.isModelExist(
                    MLLocalTranslatorModel.Factory(
                        translateLangCode
                    ).create()
                ).addOnSuccessListener { it ->
                    if (it) {
                        SendRemoteLangCode.setLangCode(translateLangCode)
                        reloadAndDetectImage()
                    }
                    onSituationSuccess(it, "LanguageCode", translateLangCode)
                }
            } else {
                SendRemoteLangCode.setLangCode(translateLangCode)
                reloadAndDetectImage()
            }

        }

    }


    private fun onSituationSuccess(it: Boolean, langCode1: String, langCode2: String) {
        if (!it) {
            val intent = Intent(this, LanguageActivity::class.java)
            SendRemoteLangCode.setLangCode(translateLangCode)
            intent.putExtra(langCode1, langCode2)
            startActivity(intent)
        }
    }


    private fun reloadAndDetectImage() {
        if (preview == null || maxHeightOfImage == null || (maxHeightOfImage == 0
                    && (preview!!.parent as View).height == 0)
        ) {
            mHandler!!.postDelayed(
                detectRunnable,
                DELAY_TIME.toLong()
            )
        } else {
            loadImageAndSetTransactor()
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.getImageButton) {
            selectLocalImage()
        } else if (view.id == R.id.back) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        reloadAndDetectImage()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        preview!!.visibility = View.GONE
        graphicOverlay!!.visibility = View.GONE
        changeImageView!!.visibility = View.VISIBLE
    }

    private fun selectLocalImage() {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        this.startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.data
            }
            reloadAndDetectImage()
        } else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_CANCELED) {
            finish()
        }
    }

    private fun loadImageAndSetTransactor() {
        if (imageUri == null) {
            return
        }
        showLoadingDialog()
        mHandler!!.postDelayed(
            myRunnable,
            TIMEOUT.toLong()
        )
        graphicOverlay!!.clear()
        imageBitmap = loadFromPath(
            this@RemoteDetectionActivity,
            imageUri!!,
            getMaxWidthOfImage()!!,
            getMaxHeightOfImage()!!
        )
        preview!!.setImageBitmap(imageBitmap)
        if (imageBitmap != null) {
            imageTransactor?.process(imageBitmap, graphicOverlay)
        }
    }

    private fun getMaxWidthOfImage(): Int? {
        if (maxWidthOfImage == null || maxWidthOfImage == 0) {
            maxWidthOfImage = (preview!!.parent as View).width
        }
        return maxWidthOfImage
    }

    private fun getMaxHeightOfImage(): Int? {
        if (maxHeightOfImage == null || maxHeightOfImage == 0) {
            maxHeightOfImage = (preview!!.parent as View).height
        }
        return maxHeightOfImage
    }

    private fun createImageTransactor() {
        imageTransactor = RemoteTextTransactor(mHandler!!)
        imageTransactor!!.getCallBack(textResultCallBack)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (imageTransactor != null) {
            imageTransactor = null
        }
        imageUri = null
        recycleBitmap(
            bitmapCopy,
            imageBitmap
        )
        mHandler?.removeCallbacksAndMessages(null)
        if (progressDialog != null) {
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
            progressDialog = null
        }
    }

    internal inner class MyCouldInfoResultCallBack : CouldInfoResultCallBack {
        override fun onSuccessForText(
            originalCameraImage: Bitmap?,
            text: MLText,
            graphicOverlay: GraphicOverlay?
        ) {
            preview!!.visibility = View.GONE
            changeImageView!!.visibility = View.VISIBLE
            bitmapCopy =
                Bitmap.createBitmap(originalCameraImage!!).copy(Bitmap.Config.ARGB_8888, true)
            cloudDataProcessor!!.setGraphicOverlay(graphicOverlay)
            cloudDataProcessor!!.setText(text)
            changeImageView!!.setImageBitmap(bitmapCopy!!)
            val canvas = Canvas(bitmapCopy!!)
            cloudDataProcessor!!.drawView(canvas, originalCameraImage)
        }
    }


    private fun showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog =
                Dialog(this@RemoteDetectionActivity, R.style.progress_dialog)
            progressDialog!!.setContentView(R.layout.dialog)
            progressDialog!!.setCancelable(false)
            progressDialog!!.window
                ?.setBackgroundDrawableResource(android.R.color.transparent)
            val msg = progressDialog!!.findViewById<TextView>(R.id.id_tv_loadingmsg)
            msg.text = this.getString(R.string.loading)
        }
        progressDialog!!.show()
    }

    companion object {
        private const val TAG = "RemoteDetectionActivity"
        private const val REQUEST_TAKE_PHOTO = 1
        private const val REQUEST_SELECT_IMAGE = 2
        private const val TIMEOUT = 20 * 1000
        private const val DELAY_TIME = 600
    }

    class SendRemoteLangCode {
        companion object {
            var langcode: String? = null
            fun getLangCode(): String? {
                return langcode
            }

            fun setLangCode(langcode: String) {
                this.langcode = langcode
            }
        }
    }

}