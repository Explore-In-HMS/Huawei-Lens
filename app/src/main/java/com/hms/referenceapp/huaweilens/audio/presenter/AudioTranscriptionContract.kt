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

package com.hms.referenceapp.huaweilens.audio.presenter

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult

interface AudioTranscriptionContract {

    interface Presenter {
        fun init()
        fun initAudioPlayer()
        fun startShortAftEngine(uri: Uri, fileUri: Uri)
        fun startLongAftEngine(uri: Uri, fileUri: Uri)
        fun pauseMedia()
        fun destroyMedia()
        fun restoreActivityState()
        fun saveActivityState()
        fun restoreSharedPrefs()
        fun resumeFragment()
    }

    interface View {
        fun fileSelectorIntent()
        fun onActivityResultSelectFile(result: ActivityResult)
        fun processSelectedFile(data: Intent?)
        fun promptPermissionRequiredDialog()
    }

}