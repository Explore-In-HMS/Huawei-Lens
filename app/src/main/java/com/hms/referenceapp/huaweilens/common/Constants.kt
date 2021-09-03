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
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hms.referenceapp.huaweilens.common

import android.Manifest
import android.os.Build
import android.os.Environment
import java.util.*

object Constants {

    /*
        Common Constants
     */
    val MANUFACTURER_BRAND = Build.MANUFACTURER.toUpperCase(Locale.ROOT)
    const val PRIMARY = "primary"
    const val LOCAL_STORAGE = "/storage/self/primary/"
    const val EXT_STORAGE = "/storage/emulated/0/"
    const val COLON = ":"
    const val RECORDING_FOLDER = "Recordings"
    val ROOT_FOLDER = Environment.getExternalStorageDirectory().absolutePath + "/Huawei-Lens/"

    /*
        Text Recognition Constants
     */
    const val GET_DATA_SUCCESS = 100
    const val GET_DATA_FAILED = 101
    const val CLOUD_TEXT_DETECTION = "Cloud Text"
    const val MODEL_TYPE = "model_type"
    const val ADD_PICTURE_TYPE = "picture_type"
    const val TYPE_TAKE_PHOTO = "take photo"
    const val TYPE_SELECT_IMAGE = "select image"


    /*
        Audio Transcription Constants
     */
    val AT_PERMISSION_LIST = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    const val SHORT_AFT_MAX_DURATION_LIMIT_MILLIS = 1000 * 60 // 1 minute
    const val LONG_AFT_MAX_DURATION_LIMIT_MILLIS = 1000 * 60 * 60 * 5  // 5 hours (up to 5 hours allowed)
    const val SHORT_AFT_MAX_FILE_SIZE_LIMIT_BYTES = 1000 * 1000 * 3 // 3MB
    const val LONG_AFT_MAX_FILE_SIZE_LIMIT_BYTES = 1000 * 1000 * 500 // 300MB
    const val RTT_MAX_DURATION_AUDIO_RECORD: Int = 1000 * 60 * 60 * 5 // 5 hours (up to 5 hours allowed)
    const val AUDIO_PLAYER_RUN_PER_MILLIS = 100  // Seek bar sensitivity
    const val AUDIO_PLAYER_SEEK_LEVEL_1 = 1500 // Seeking level for file duration above 10 seconds
    const val AUDIO_PLAYER_SEEK_LEVEL_2 = 500 // Seeking level for file duration below 10 seconds

}