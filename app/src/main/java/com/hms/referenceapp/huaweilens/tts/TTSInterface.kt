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

package com.hms.referenceapp.huaweilens.tts

import android.widget.Button
import android.widget.ImageButton
import com.huawei.hms.mlsdk.tts.MLTtsEngine

interface TTSInterface {

    interface TPresenter {
        fun trigger(txt: String)
        fun importDetectLanguage(shortTxt: String, longTxt: String)
        fun manualDetectLanguage(txt: String)
        fun init()
        fun giveText(txt: String)
        fun setConfigs(lng: String, gender: String)
        fun giveCurrentLng(): String?
    }

    interface TView {
        fun useEngine(mlTtsEngine: MLTtsEngine?)
        fun ttsButton(): ImageButton?
        fun isMale(xy: Boolean): Boolean?
        fun isFemale(xx: Boolean): Boolean?
        fun fromFile(): Boolean
        fun maleButtonP(): Button?
        fun femaleButtonP(): Button?
        fun sourceText(): String?
        fun makeItClickable(btn: Button)
        fun listen(str: String)
        fun selectSpeaker(lng: String)
        fun setVolume(): Float
        fun setSpeed(): Float
        fun setMyText(str: String)
    }
}