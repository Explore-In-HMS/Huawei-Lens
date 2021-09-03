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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hms.referenceapp.huaweilens.dsc.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.hms.referenceapp.huaweilens.R


class ChoosePictureDialog(context: Context, var type: Int) :
    Dialog(context, R.style.MyDialogStyle), View.OnClickListener {
    private lateinit var tvSelectImage: TextView
    private lateinit var tvExtend: TextView
    private var clickListener: ClickListener? = null

    interface ClickListener {
        /**
         * Take picture
         */

        /**
         * Select picture from local
         */
        fun selectImage()

        /**
         * Extension method
         */
        fun doExtend()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    private fun initViews() {
        val inflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams") val view: View =
            inflater.inflate(R.layout.dialog_add_picturedsc, null)
        this.setContentView(view)

        tvSelectImage = view.findViewById(R.id.select_image)
        tvExtend = view.findViewById(R.id.extend)
        if (type == TYPE_CUSTOM) {
            tvExtend.text = "Cancel"
        }

        tvSelectImage.setOnClickListener(this)
        tvExtend.setOnClickListener(this)
        setCanceledOnTouchOutside(true)
        val dialogWindow = this.window
        if (dialogWindow != null) {
            val layoutParams = dialogWindow.attributes
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.gravity = Gravity.BOTTOM
            dialogWindow.attributes = layoutParams
        }
    }

    fun setClickListener(clickListener: ClickListener?) {
        if (clickListener != null) {
            this.clickListener = clickListener
        }
    }

    override fun onClick(v: View) {
        dismiss()
        if (clickListener == null) {
            return
        }
        when (v.id) {
            R.id.select_image -> clickListener!!.selectImage()
            R.id.extend -> clickListener!!.doExtend()
        }
    }

    companion object {
        const val TYPE_NORMAL = 1
        const val TYPE_CUSTOM = 2
    }
}