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

package com.hms.referenceapp.huaweilens.dsc.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.FileActivity
import com.hms.referenceapp.huaweilens.dsc.contract.SaverActivityContract
import com.hms.referenceapp.huaweilens.dsc.presenter.SaverPresenter
import com.hms.referenceapp.huaweilens.main.MenuActivity
import java.text.SimpleDateFormat
import java.util.*


class SaverActivity : AppCompatActivity(), SaverActivityContract.IView {

    private var presenter: SaverPresenter? = null
    lateinit var switchTxt: CheckBox
    lateinit var switchWord: CheckBox
    lateinit var switchWordImage: CheckBox
    lateinit var switchPdf: CheckBox
    lateinit var switchPdfImage: CheckBox
    lateinit var switchGallery: CheckBox
    lateinit var buttonOk: ImageView
    lateinit var buttonCancel: ImageView
    lateinit var progress: ProgressDialog
    private var textFail: Boolean = false
    private var checkControl: Boolean = false


    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_file)

        presenter = SaverPresenter(this)
        init()



        buttonOk.setOnClickListener {

            progress = ProgressDialog(this)
            progress.setTitle("Results are loading, please wait")
            progress.setCancelable(false)
            progress.show()

            DoAsync {
                createFiles()

                if (checkControl == false) {
                    progress.dismiss()
                    Toast.makeText(
                        this, "Please select folder type to save.",
                        Toast.LENGTH_LONG
                    ).show()


                } else if (textFail) {
                    progress.dismiss()
                    Toast.makeText(
                        this, "Saving text file is unavailable due to text recognition failure",
                        Toast.LENGTH_LONG
                    ).show()
                    textFail = false


                } else if (!switchTxt.isChecked && !switchWord.isChecked && !switchWordImage.isChecked &&
                    !switchPdf.isChecked &&
                    !switchPdfImage.isChecked && switchGallery.isChecked
                ) {

                    progress.dismiss()
                    val intent = Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            "content://media/internal/images/media"
                        )
                    )
                    startActivity(intent)

                    checkControl = false
                } else {
                    progress.dismiss()
                    MenuActivity.SendImage.ctrl = 1
                    val intent = Intent(this, FileActivity::class.java)
                    startActivity(intent)
                    checkControl = false
                }
            }

        }








        buttonCancel.setOnClickListener {
            onBackPressed()
        }


    }

    class DoAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        init {
            execute()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }

    private fun createFiles() {

        if (switchTxt.isChecked) {
            checkControl = true
            if (PreviewActivity.ocrContoller) {
                val txtName = SimpleDateFormat("yyyy-dd-M-HH-mm-ss'.txt'").format(Date())
                presenter?.savefile(txtName, PreviewActivity.text.stringValue)
            } else {
                textFail = true
            }
        }
        if (switchWord.isChecked) {
            checkControl = true
            if (PreviewActivity.ocrContoller) {
                val txtName = SimpleDateFormat("yyyy-dd-M-HH-mm-ss'.docx'").format(Date())
                presenter?.saveDocx(txtName, PreviewActivity.text.blocks)
            } else {
                textFail = true
            }
        }
        if (switchWordImage.isChecked) {
            checkControl = true
            val txtName = SimpleDateFormat("yyyy-dd-M-HH-mm-ss'-Image.docx'").format(Date())
            presenter?.saveDocxImage(PreviewActivity.bitmap, "new.png", txtName)
        }
        if (switchPdf.isChecked) {
            checkControl = true
            if (PreviewActivity.ocrContoller) {
                val txtName = SimpleDateFormat("yyyy-dd-M-HH-mm-ss'.pdf'").format(Date())

                presenter?.savePdf(txtName, PreviewActivity.text.blocks)

            } else {
                textFail = true
            }

        }

        if (switchPdfImage.isChecked) {
            checkControl = true
            val txtName = SimpleDateFormat("yyyy-dd-M-HH-mm-ss'-Image.pdf'").format(Date())
            presenter?.saveImagePdf(PreviewActivity.bitmap, txtName)


        }
        if (switchGallery.isChecked) {
            checkControl = true
            val name = SimpleDateFormat("yyyy-dd-M-HH-mm-ss").format(Date())
            presenter?.saveGallery(
                PreviewActivity.bitmap,
                Bitmap.CompressFormat.PNG,
                100,
                name
            )

        }
    }

    private fun init() {
        switchTxt = findViewById(R.id.cb_txt)
        switchWord = findViewById(R.id.cb_doctext)
        switchWordImage = findViewById(R.id.cb_docimage)
        switchPdf = findViewById(R.id.cb_pdftext)
        switchPdfImage = findViewById(R.id.cb_pdfimage)
        switchGallery = findViewById(R.id.cb_gallery)
        buttonOk = findViewById(R.id.buttonok)
        buttonCancel = findViewById(R.id.buttoncancel)


    }

    override fun getViewActivity(): Activity {
        return this
    }


    override fun showProgressDialog() {
        progress.show()
    }

    override fun dismissProgressDialog() {
        progress.dismiss()
    }


}