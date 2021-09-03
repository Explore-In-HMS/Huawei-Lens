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


package com.hms.referenceapp.huaweilens.trt.helper

import android.text.TextUtils
import android.util.Log
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.langdetect.MLLangDetectorFactory
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetector
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetectorSetting
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator


class DetectAndTranslateHelper {

    companion object{
        private  var  fromLanguageCode: String=""
        private  var result:String=""
        private var mlLocalLangDetector: MLLocalLangDetector?=null


        private fun createLocalInstance(){
            val factory = MLLangDetectorFactory.getInstance()
            val setting =
                MLLocalLangDetectorSetting.Factory() // Set the minimum confidence threshold for language detection.
                    .setTrustedThreshold(0.01f)
                    .create()
            mlLocalLangDetector = factory.getLocalLangDetector(setting)

        }

        fun detectAndTranslateLanguageReturn(sourceText: String):String {

            createLocalInstance()
            try {
                fromLanguageCode = mlLocalLangDetector!!.syncFirstBestDetect(sourceText)
            } catch (e: MLException) {
                Log.d("denemeDetect",e.message.toString())
            }
            mlLocalLangDetector!!.stop()

            return fromLanguageCode
        }


        fun translateTextRemoteWithReturn(sourceText: String, fromLang: String, toLang: String):String {
            val setting: MLLocalTranslateSetting =
                MLLocalTranslateSetting.Factory()
                    .setSourceLangCode(fromLang)
                    .setTargetLangCode(toLang)
                    .create()

            val  mlLocalTranslator: MLLocalTranslator = MLTranslatorFactory.getInstance().getLocalTranslator(setting)

            result = if(!TextUtils.isEmpty(sourceText)){
                try {
                    val output:String=
                        mlLocalTranslator.syncTranslate(sourceText)
                    output
                }catch (e: MLException) {
                    e.message?.let { Log.i("TranslateERROR", it) }
                    sourceText
                }

            } else {
                "null"
            }
            mlLocalTranslator.stop()
            return  result
        }

    }
}

