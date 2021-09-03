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
import android.content.SharedPreferences
import android.util.Log
import android.view.View

import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.hms.referenceapp.huaweilens.common.language.LanguageActivity
import com.hms.referenceapp.huaweilens.common.translate.GetLanguageArray
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.interfaces.ClassificationInterface
import com.hms.referenceapp.huaweilens.odt.activities.ClassificationActivity
import com.hms.referenceapp.huaweilens.odt.adapters.RecyclerViewAdapter
import com.hms.referenceapp.huaweilens.trt.views.ScrollChoice
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.classification.MLImageClassification
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer
import com.huawei.hms.mlsdk.classification.MLLocalClassificationAnalyzerSetting
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel
import kotlinx.android.synthetic.main.activity_classification.*
import kotlinx.android.synthetic.main.activity_classification.img_check
import kotlinx.android.synthetic.main.activity_classification.img_close
import kotlinx.android.synthetic.main.activity_classification.ln_collapse
import kotlinx.android.synthetic.main.activity_classification.ln_expand
import kotlinx.android.synthetic.main.activity_classification.scroll_choice
import kotlinx.android.synthetic.main.activity_classification.txt_language_in
import kotlinx.android.synthetic.main.activity_classification.txt_language_out
import java.util.Collections.reverse
import kotlin.collections.ArrayList

class ClassificationPresenter(var view: ClassificationActivity) :
    ClassificationInterface.ClassificatonPresenter {
    val listResults: MutableList<ClassificationPresenter.ClassificationItems> = ArrayList()
    private lateinit var mImageview: ImageView
    private lateinit var languageCode: String
    private var myLanguage: String = ""

    var list: Array<String?>? = null
    private var langCode: String = ""
    private var languagePosition: String = ""
    private var languageName: String = ""
    private var mLanguageArray: GetLanguageArray =
        GetLanguageArray(GetLanguageArray.LOCAL, view, true)
            .sortByDownloadedModels()

    // Method 1: Use customized parameter settings for on-device recognition.
    private var deviceSetting: MLLocalClassificationAnalyzerSetting =
        MLLocalClassificationAnalyzerSetting.Factory()
            .setMinAcceptablePossibility(0.6f)
            .create()
    private var deviceAnalyzer: MLImageClassificationAnalyzer =
        MLAnalyzerFactory.getInstance().getLocalImageClassificationAnalyzer(
            deviceSetting
        )


    val madapter: RecyclerViewAdapter = RecyclerViewAdapter(listResults)

    private var languageString: String = "none"
    private val manager: MLLocalModelManager = MLLocalModelManager.getInstance()

    private var prefs: SharedPreferences? = null

    @SuppressLint("SetTextI18n")
    fun init() {
        mImageview = view.imgTourismHr
        mImageview.setImageBitmap(MenuActivity.SendImage.getBitmap())

        languageCode = prefs!!.getString("languageCode", "none").toString()
        if (languageCode == "none") {
            val builder = AlertDialog.Builder(view)
            builder.setTitle("Language Selection")
            builder.setMessage("Please select a language for translation")

            builder.setPositiveButton("OK") { dialog, which ->
            }
            builder.show()
        } else {
            languageName = prefs!!.getString("position", "none").toString()

            view.txt_language_out.text = languageName
            view.textResult.text = "Result - $languageName"

            if (languageCode != "en"){

            com.hms.referenceapp.huaweilens.common.language.manager.isModelExist(
                MLLocalTranslatorModel.Factory(
                    languageCode
                ).create()
            ).addOnSuccessListener {
                if (!it) {
                    val builder = AlertDialog.Builder(view)
                    builder.setTitle("Download Model")
                    builder.setMessage("Please download a required model for translation")
                    builder.setPositiveButton("OK") { dialog, which ->
                        val intent = Intent(view, LanguageActivity::class.java)
                        intent.putExtra("LanguageCode", languageCode)
                        view.startActivity(intent)
                    }
                    builder.show()
                }
            }
            }

            val setting: MLLocalTranslateSetting =
                MLLocalTranslateSetting.Factory()
                    .setSourceLangCode("en")
                    .setTargetLangCode(languageCode)
                    .create()
            val mlLocalTranslator: MLLocalTranslator =
                MLTranslatorFactory.getInstance().getLocalTranslator(setting)

            val frame: MLFrame = MLFrame.fromBitmap(MenuActivity.SendImage.getBitmap())
            val task: Task<List<MLImageClassification>> = deviceAnalyzer.asyncAnalyseFrame(frame)
            task.addOnSuccessListener {
                Log.d("tag", "test")
                listResults.clear()
                if (it.isEmpty()) {
                    view.recyclerView.visibility = View.GONE
                    view.empty_view.visibility = View.VISIBLE
                } else {

                    val listPossibility = it.sortedBy { it.possibility }
                    reverse(listPossibility)
                    for (i in listPossibility.indices) {

                        val task =
                            mlLocalTranslator.asyncTranslate(listPossibility[i].name)

                        task.addOnSuccessListener { s ->
                            Log.d("Tag", "e.getMessage()")
                            listResults.add(
                                ClassificationPresenter.ClassificationItems(
                                    listPossibility[i].name,
                                    s,
                                    listPossibility[i].possibility
                                )
                            )
                            view.empty_view.visibility = View.GONE
                            view.recyclerView.visibility = View.VISIBLE

                            listResults.sortBy { it.location }
                            reverse(listResults)
                            madapter.notifyDataSetChanged()

                            mlLocalTranslator.stop()
                        }.addOnFailureListener {
                            Log.d("Tag", "e.getMessage()")
                            view.recyclerView.visibility = View.GONE
                            view.empty_view.visibility = View.VISIBLE
                        }
                    }

                }
            }

            view.recyclerView.adapter = madapter
        }

        mLanguageArray = GetLanguageArray(GetLanguageArray.LOCAL, view, false)

        list = mLanguageArray.getNames()

        view.scroll_choice.addItems(
            list!!.toList(),
            list!!.toList().indexOf(view.txt_language_out.text)
        )

        view.scroll_choice.setOnItemSelectedListener(ScrollChoice.OnItemSelectedListener { scrollChoice, position, name ->
            view.txt_language_in.text = name
            langCode = mLanguageArray.getLanguages()[position].iso6391
            languagePosition = mLanguageArray.getLanguages()[position].name
        })

        view.ln_collapse.setOnClickListener {
            it.visibility = View.GONE
            view.ln_expand.visibility = View.VISIBLE
            view.txt_language_in.text = view.txt_language_out.text
            myLanguage = view.txt_language_in.text.toString()
            view.scroll_choice.addItems(
                list!!.toList(),
                list!!.toList().indexOf(view.txt_language_out.text)
            )
        }

        view.img_close.setOnClickListener {
            view.ln_collapse.visibility = View.VISIBLE
            view.ln_expand.visibility = View.GONE
            view.txt_language_out.text = myLanguage
        }

        view.img_check.setOnClickListener {
            view.ln_collapse.visibility = View.VISIBLE
            view.ln_expand.visibility = View.GONE
            view.txt_language_out.text = view.txt_language_in.text

            if (langCode == "en" || langCode == "zh") {
                setTranslateLanguage(langCode, languagePosition, false)
            } else {
                manager.isModelExist(
                    MLLocalTranslatorModel.Factory(
                        langCode
                    ).create()
                ).addOnSuccessListener {
                    if (it) {
                        setTranslateLanguage(langCode, languagePosition, false)
                    } else {
                        setTranslateLanguage(langCode, languagePosition, true)
                        val intent = Intent(view, LanguageActivity::class.java)
                        intent.putExtra("LanguageCode", langCode)
                        view.startActivity(intent)
                    }
                }
            }


        }
        view.back_imagebutton.setOnClickListener {
            view.onBackPressed()
        }
    }

    override fun downloadModels() {
        prefs = view.getSharedPreferences("PREFS_NAME", 0)

        init()
    }

    private fun setTranslateLanguage(localName: String, position: String, isIntent: Boolean) {
        languageString = localName


        val editor = prefs!!.edit()
        editor.putString("languageCode", languageString)
        editor.putString("position", position)
        editor.apply()
        if (!isIntent) {
            init()
        }

    }

    data class ClassificationItems(
        var firstName: String,
        var lastName: String,
        var location: Float
    )
}