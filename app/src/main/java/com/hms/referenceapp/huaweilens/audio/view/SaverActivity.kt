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

package com.hms.referenceapp.huaweilens.audio.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.presenter.SaverActivityContract
import com.hms.referenceapp.huaweilens.audio.presenter.SaverPresenter
import com.hms.referenceapp.huaweilens.common.FileActivity


class SaverActivity : AppCompatActivity(), SaverActivityContract.View {

    private var presenter: SaverPresenter? = null
    private lateinit var switchTxt: CheckBox
    private lateinit var switchWord: CheckBox
    private lateinit var switchPdf: CheckBox
    private lateinit var buttonOk: ImageView
    private lateinit var buttonCancel: ImageView
    private var checkControl: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.at_download_file)

        presenter = SaverPresenter(this)
        presenter?.bindFileName(intent.getStringExtra("originalFileName")!!)
        presenter?.showTranslation = intent.getBooleanExtra("showTranslation", false)
        init()

        buttonOk.setOnClickListener {

            if (switchTxt.isChecked) {
                checkControl = true
                presenter?.saveTxt()
            }
            if (switchWord.isChecked) {
                checkControl = true
                presenter?.saveDoc()
            }
            if (switchPdf.isChecked) {
                checkControl = true
                presenter?.savePdf()
            }

            if (!checkControl) {
                Toast.makeText(
                    this, "Please select a format to export results.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val intent = Intent(this, FileActivity::class.java)
                startActivity(intent)
                checkControl = false
            }
        }

        buttonCancel.setOnClickListener {
            onBackPressed()
        }

    }

    private fun init() {
        switchTxt = findViewById(R.id.cb_txt)
        switchWord = findViewById(R.id.cb_doctext)
        switchPdf = findViewById(R.id.cb_pdftext)
        buttonOk = findViewById(R.id.buttonok)
        buttonCancel = findViewById(R.id.buttoncancel)
    }


    override fun getViewActivity(): Activity {
        return this
    }

}