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

package com.hms.referenceapp.huaweilens.dsc.contract

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import com.huawei.hms.mlsdk.text.MLText

interface SaverActivityContract {

    interface IView {
        fun getViewActivity(): Activity
        fun showProgressDialog()
        fun dismissProgressDialog()
    }

    interface IPresenter {
        fun savefile(filename: String, content: String)
        fun savePdf(filename: String, content: MutableList<MLText.Block>)
        fun saveImagePdf(bitmap: Bitmap, filename: String)
        fun saveDocx(filename: String, content: MutableList<MLText.Block>)
        fun saveDocxImage(bitmap: Bitmap, filenameImg: String, filename: String)
        fun saveGallery(src: Bitmap, format: Bitmap.CompressFormat?, quality: Int, title: String)

    }
}