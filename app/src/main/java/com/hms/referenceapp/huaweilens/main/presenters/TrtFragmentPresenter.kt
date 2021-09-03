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

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.view.View.*
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.common.language.LanguageActivity
import com.hms.referenceapp.huaweilens.common.translate.GetLanguageArray
import com.hms.referenceapp.huaweilens.main.fragments.TRTNewFragment
import com.hms.referenceapp.huaweilens.main.interfaces.TrtFragmentInterface
import com.hms.referenceapp.huaweilens.trt.activity.RemoteDetectionActivity
import com.hms.referenceapp.huaweilens.trt.camera.CameraSource
import com.hms.referenceapp.huaweilens.trt.camera.CameraSourcePreview
import com.hms.referenceapp.huaweilens.trt.processor.TextRecognitionProcessor
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel
import kotlinx.android.synthetic.main.fragment_trt.*


class TrtFragmentPresenter(var view: TRTNewFragment) : TrtFragmentInterface.CameraPresenter,

    LifecycleOwner {
    var translateList: Array<String?>? = null
    private var myTranslateList: MutableList<String?>? = null
    private var mySourceList: MutableList<String?>? = null
    private var leftText: String = ""
    private var rightText: String = ""
    private var isChecked: Boolean = false
    private var translateLangCode: String = ""
    private var sourceLangCode: String = ""
    val manager = MLLocalModelManager.getInstance()
    private var mLanguageArray: GetLanguageArray? = null
    var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var isFlashOn: Boolean = false
    private var isFirstClick: Boolean = true


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initButtonCallbacks(cameraSource: CameraSource) {
        view.ln_flash.setOnClickListener {
            clickOnFlash(cameraSource)
        }

        view.text_imageSwitch.setOnClickListener {
            val intent = Intent(view.requireActivity(), RemoteDetectionActivity::class.java)
            intent.putExtra(
                Constants.ADD_PICTURE_TYPE,
                Constants.TYPE_SELECT_IMAGE
            )
            view.requireActivity().startActivity(intent)
        }

        view.takePicture.setOnClickListener {
            clickTakePicture(cameraSource)

        }

        view.ln_collapse.setOnClickListener {
            SendLangCode.setisOpenedSpinner(true)
            view.ln_expand.visibility = VISIBLE
            view.ln_collapse.background = null
            leftText = view.txt_language_out_left.text.toString()
            rightText = view.txt_language_out_right.text.toString()
            view.scroll_choice_right.addItems(
                myTranslateList,
                myTranslateList!!.indexOf(view.txt_language_out_right.text)
            )
            view.scroll_choice_left.addItems(
                mySourceList,
                mySourceList!!.indexOf(view.txt_language_out_left.text)
            )
            view.imagebutton_language.visibility = GONE
        }

        view.img_close.setOnClickListener {
            SendLangCode.setisOpenedSpinner(false)
            view.ln_collapse.visibility = VISIBLE
            view.ln_collapse.background =
                view.requireContext().getDrawable(R.drawable.transcription_type_radiogroup_trt)
            view.ln_expand.visibility = GONE
            view.txt_language_out_left.text = leftText
            view.txt_language_out_right.text = rightText
            view.imagebutton_language.visibility = VISIBLE
        }

        view.imagebutton_language.setOnClickListener {
            val intent = Intent(view.requireActivity(), LanguageActivity::class.java)
            view.requireActivity().startActivity(intent)
        }

        view.img_check.setOnClickListener {
            SendLangCode.setisOpenedSpinner(false)
            view.ln_collapse.visibility = VISIBLE
            view.ln_collapse.background =
                view.requireContext().getDrawable(R.drawable.transcription_type_radiogroup_trt)
            view.ln_expand.visibility = GONE
            view.imagebutton_language.visibility = VISIBLE
            val sourceCode = TextRecognitionProcessor.SendSourceCode.getSourceCode()
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
                        manager.isModelExist(
                            MLLocalTranslatorModel.Factory(sourceLangCode).create()
                        ).addOnSuccessListener {
                            onSituationSuccess(it,"sourceLanguageCode",sourceLangCode)
                        }
                    } else {
                        manager.isModelExist(
                            MLLocalTranslatorModel.Factory(sourceLangCode).create()
                        ).addOnSuccessListener {
                            val intent = Intent(view.requireContext(), LanguageActivity::class.java)
                            if (!it) {
                                intent.putExtra("sourceLanguageCode", sourceLangCode)
                            }
                            intent.putExtra("LanguageCode", translateLangCode)
                            view.requireActivity().startActivity(intent)
                        }
                    }
                }

            } else if (translateLangCode == "en" && sourceLangCode != "en") {
                manager.isModelExist(
                    MLLocalTranslatorModel.Factory(
                        sourceLangCode
                    ).create()
                ).addOnSuccessListener { it ->
                    onSituationSuccess(it,"sourceLanguageCode",sourceLangCode)
                }
            } else if (sourceLangCode == "en" && translateLangCode != "en") {
                manager.isModelExist(
                    MLLocalTranslatorModel.Factory(
                        translateLangCode
                    ).create()
                ).addOnSuccessListener { it ->
                    onSituationSuccess(it,"LanguageCode",translateLangCode)
                }
            }
            SendLangCode.setLangCode(translateLangCode)
        }
    }

    private fun onSituationSuccess(it: Boolean, langCode1: String, langCode2: String) {
        if (!it) {
            val intent =
                Intent(view.requireContext(), LanguageActivity::class.java)
            intent.putExtra(langCode1, langCode2)
            view.requireActivity().startActivity(intent)
        }
    }

    private fun clickTakePicture(cameraSource: CameraSource) {
        if (isFirstClick) {
            cameraSource.camera?.stopPreview()
            view.takePicture.setImageResource(R.drawable.ic_play_arrow_white_48dp)
            view.ln_flash.isClickable = false
            view.text_imageSwitch.isClickable = false
            isFirstClick = false
        } else {
            cameraSource.camera?.startPreview()
            view.takePicture.setImageBitmap(null)
            view.ln_flash.isClickable = true
            view.text_imageSwitch.isClickable = true
            isFirstClick = true
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun clickOnFlash(cameraSource: CameraSource) {
        try {
            val mCamera = cameraSource.camera
            if (!isFlashOn && view.requireContext().packageManager.hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH
                )
            ) {
                val p = mCamera!!.parameters
                p.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                mCamera.parameters = p
                mCamera.startPreview()
                isFlashOn = true
                view.ln_flash.alpha = 1f
                view.flash_text?.text = view.context?.getString(R.string.trtLightOff)
                view.flash?.setImageDrawable(
                    view.requireContext().getDrawable(R.drawable.ic_lightbulb_on)
                )
            } else if (isFlashOn && view.requireContext().packageManager.hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH
                )
            ) {
                val p = mCamera!!.parameters
                p.flashMode = Camera.Parameters.FLASH_MODE_OFF
                mCamera.parameters = p
                mCamera.startPreview()
                isFlashOn = false
                view.ln_flash.alpha = 0.5f
                view.flash_text?.text = view.context?.getString(R.string.trtLightOn)
                view.flash?.setImageDrawable(
                    view.requireContext().getDrawable(R.drawable.ic_lightbulb)
                )
            }
        } catch (e: Exception) {
            e.message
            Toast.makeText(
                view.requireContext(), "Exception flashLightOn()",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun initSpinner() {

        mLanguageArray = GetLanguageArray(GetLanguageArray.LOCAL, view.requireContext(), false)

        translateList = mLanguageArray!!.getNames()
        myTranslateList = translateList!!.toMutableList()
        myTranslateList!!.add(0, "Select language")
        SendLangCode.setLangCode("none")
        view.scroll_choice_right.addItems(
            myTranslateList,
            myTranslateList!!.indexOf(view.txt_language_out_right.text)
        )
        view.scroll_choice_right.setOnItemSelectedListener { scrollChoice, position, name ->
            view.txt_language_out_right.text = name
            translateLangCode =  ""
            if (position != 0) {
                translateLangCode = mLanguageArray!!.getLanguages()[position - 1].iso6391
            }

        }

        val sourceList = mLanguageArray!!.getNames()
        mySourceList = sourceList.toMutableList()
        mySourceList!!.add(0, "(Auto-detect)")
        TextRecognitionProcessor.SendSourceCode.setSourceCode("none")
        view.scroll_choice_left.addItems(
            mySourceList,
            mySourceList!!.indexOf(view.txt_language_out_right.text)
        )
        view.scroll_choice_left.setOnItemSelectedListener { scrollChoice, position, name ->
            view.txt_language_out_left.text = name
            sourceLangCode = ""
            if (position != 0) {
                sourceLangCode = mLanguageArray!!.getLanguages()[position - 1].iso6391
            }
        }

    }


    override fun initOverlay(graphicOverlay: GraphicOverlay?) {
        this.graphicOverlay = graphicOverlay
    }

    override fun initPreview(preview: CameraSourcePreview?) {
        this.preview = preview
    }

    override fun getLifecycle(): Lifecycle {
        return view.lifecycle
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun closeSpinner() {
        view.ln_expand.visibility = GONE
        view.ln_collapse.background =
            view.requireContext().getDrawable(R.drawable.transcription_type_radiogroup_trt)
        view.imagebutton_language.visibility = VISIBLE
    }

    class SendLangCode {
        companion object {
            var langcode: String? = null
            private var isOpenedSpinner: Boolean=false

            fun getisOpenedSpinner(): Boolean? {
                return isOpenedSpinner
            }

            fun setisOpenedSpinner(isOpenedSpinner: Boolean) {
                this.isOpenedSpinner = isOpenedSpinner
            }

            fun getLangCode(): String? {
                return langcode
            }

            fun setLangCode(langcode: String) {
                this.langcode = langcode
            }
        }
    }
}