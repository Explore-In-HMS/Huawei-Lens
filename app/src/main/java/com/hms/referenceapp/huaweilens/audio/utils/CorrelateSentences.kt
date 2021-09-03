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

import android.annotation.SuppressLint
import com.hms.referenceapp.huaweilens.audio.entity.ResultSentence
import com.huawei.hms.mlsdk.aft.MLAftConstants
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionConstants
import java.util.regex.Pattern

class CorrelateSentences(private var sentences: MutableList<ResultSentence>?) {

    var lang: String? = null

    @SuppressLint("DefaultLocale")
    fun correlate(sentence: ResultSentence) {
        /*
            Sentence object may return a punctuation symbol
         */
        if (!Pattern.matches("\\p{Punct}", sentence.text)) {
            if (lang != MLSpeechRealTimeTranscriptionConstants.LAN_ZH_CN && lang != MLAftConstants.LANGUAGE_ZH) {
                // append sentence to previous one if it ends with comma
                // append sentence if it does not end with dot
                if (!sentences.isNullOrEmpty() && (sentences?.last()!!.text.endsWith(",") || !sentences?.last()!!.text.endsWith(
                        "."
                    ))
                ) {
                    sentences!!.last().text += " " + sentence.text
                    sentences!!.last().endTime = sentence.endTime
                } else {
                    sentence.text = sentence.text.capitalize()
                    sentences?.add(sentence)
                }
            } else {
                sentences?.add(sentence) // for chinese inputs
            }

        } else {
            // append punctuation to previous sentence
            if (!sentences.isNullOrEmpty()) sentences?.last()!!.text += sentence.text
        }
    }

}