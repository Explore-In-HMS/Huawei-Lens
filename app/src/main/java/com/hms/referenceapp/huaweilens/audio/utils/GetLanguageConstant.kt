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

import com.huawei.hms.mlsdk.aft.MLAftConstants
import com.huawei.hms.mlsdk.speechrtt.MLSpeechRealTimeTranscriptionConstants

object GetLanguageConstant {

    val constantsAft = arrayOf(
        MLAftConstants.LANGUAGE_EN_US,
        MLAftConstants.LANGUAGE_ZH
    )

    val constantsRtt = arrayOf(
        MLSpeechRealTimeTranscriptionConstants.LAN_EN_US,
        MLSpeechRealTimeTranscriptionConstants.LAN_ZH_CN,
        MLSpeechRealTimeTranscriptionConstants.LAN_FR_FR,
        MLSpeechRealTimeTranscriptionConstants.LAN_DE_DE,
        MLSpeechRealTimeTranscriptionConstants.LAN_EN_IN,
        MLSpeechRealTimeTranscriptionConstants.LAN_ES_ES
    )

    val iso = arrayOf("en", "zh", "fr", "de", "it", "es")
    
}