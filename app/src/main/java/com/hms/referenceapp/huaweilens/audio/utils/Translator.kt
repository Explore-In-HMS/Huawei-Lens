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

package com.hms.referenceapp.huaweilens.audio.utils

import android.util.Log
import com.hms.referenceapp.huaweilens.audio.entity.ResultSentence
import com.hms.referenceapp.huaweilens.odt.App
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel

class Translator {

    lateinit var sourceLanguage: String
    lateinit var targetLanguage: String
    var listener: (()->Unit)? = null
    var errorListener: (()->Unit)? = null
    private var errorPrompted = false
    var returnSentences: MutableList<ResultSentence> = mutableListOf()
    private var manager: MLLocalModelManager = MLLocalModelManager.getInstance()

    init {
        //set HMS ML Kit API KEY
        MLApplication.getInstance().apiKey = App.API_KEY

    }

    fun translateBatch(sentences: MutableList<ResultSentence>?) {

        val setting: MLLocalTranslateSetting = MLLocalTranslateSetting.Factory()
            .setSourceLangCode(sourceLanguage)
            .setTargetLangCode(targetLanguage)
            .create()
        val mlLocalTranslator: MLLocalTranslator = MLTranslatorFactory.getInstance().getLocalTranslator(
            setting
        )

        manager.isModelExist(
            MLLocalTranslatorModel.Factory(targetLanguage).create()
        ).addOnSuccessListener { _ ->

            returnSentences.clear()
            sentences?.forEach {
                returnSentences.add(ResultSentence(it.text, it.startTime, it.endTime))
            }
            var count = 0
            sentences?.forEachIndexed { index, s ->
                // sourceText: text to be translated, with up to 5000 characters
                val task = mlLocalTranslator.asyncTranslate(s.text)
                task.addOnSuccessListener {
                    returnSentences[index] = ResultSentence(it, s.startTime, s.endTime)
                    count++
                    if(count == sentences.size) {
                        listener?.invoke()
                    }
                    // Processing logic for recognition success.
                }.addOnFailureListener { e ->
                    // Processing logic for recognition failure.
                    try {
                        val mlException = e as MLException
                        // Obtain the result code. You can process the result code and customize respective messages displayed to users.
                        val errorCode = mlException.errCode
                        // Obtain the error information. You can quickly locate the fault based on the result code.
                        val errorMessage = mlException.message
                        Log.e("translate", "translate: $errorMessage ($errorCode)")
                        if(!errorPrompted) {
                            errorListener?.invoke()
                            errorPrompted = true
                        }
                    } catch (error: Exception) {
                        // Handle the conversion error.
                    }
                }
            }
        }.addOnFailureListener {
            if(!errorPrompted) {
                errorListener?.invoke()
                errorPrompted = true
            }
            Log.e("translate", "translate: target model does not exist.")
        }




    }
}