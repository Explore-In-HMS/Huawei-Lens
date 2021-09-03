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

package com.hms.referenceapp.huaweilens.common.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.hms.referenceapp.huaweilens.R
import java.util.*


class DocumentShare(private var ctx: Context, private var uri: Uri) {

    private var shareIntent: Intent = Intent()

    init {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        shareIntent.apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, ctx.resources.getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, ctx.resources.getString(R.string.file_share_message))
        }
    }

    fun share() {
        when (uri.toString().substringAfterLast(".").toLowerCase(Locale.ROOT)) {
            "txt" -> {
                shareIntent.type = "text/plain"
            }
            "pdf" -> {
                shareIntent.type = "application/pdf"
            }
            "docx" -> {
                shareIntent.type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            }
            "xlsx" -> {
                shareIntent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            }
        }
        ctx.startActivity(shareIntent)
    }
}