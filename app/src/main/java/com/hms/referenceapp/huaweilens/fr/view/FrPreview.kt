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

package com.hms.referenceapp.huaweilens.fr.view

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.dsc.cropper.CropImage
import com.hms.referenceapp.huaweilens.dsc.view.PreviewActivity
import com.hms.referenceapp.huaweilens.fr.contract.FrPreviewContract
import com.hms.referenceapp.huaweilens.fr.presenter.FrPreviewPresenter
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File


class FrPreview : AppCompatActivity(), FrPreviewContract.View {

    private var presenter: FrPreviewPresenter? = null
    private lateinit var check: ImageView
    private lateinit var ignore: ImageView
    private lateinit var photo: ImageView
    private lateinit var edit: ImageView

    private lateinit var progressbar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fr_preview)
        presenter = FrPreviewPresenter(this)
        initView()
        imagePrev=MenuActivity.SendImage.getBitmap()!!
        preview_uri = getImageUri(imagePrev,Bitmap.CompressFormat.JPEG,100)!!
        photo.setImageBitmap(imagePrev)


        check.setOnClickListener {

            progressbar.show()
            presenter?.initFormRecognition(imagePrev)
        }

        ignore.setOnClickListener {
            progressbar.dismiss()
            onBackPressed()

        }

        edit.setOnClickListener {
            CropImage.activity(preview_uri)
                .start(this)
        }


    }



    override fun initView() {
        check = findViewById(R.id.fr_check)
        ignore = findViewById(R.id.fr_ignore)
        photo = findViewById(R.id.fr_preview)
        edit = findViewById(R.id.fr_editer)
        progressbar = ProgressDialog(this)
        progressbar.setCanceledOnTouchOutside(false)
        progressbar.setMessage("Results are loading, please wait")
    }

    override fun getViewActivity(): Activity {
        return this
    }

    override fun startActivityFResult(intent: Intent, requestCode: Int) {
        startActivityForResult(intent, requestCode)
    }

    override fun showProgressDialog() {
        progressbar.show()
    }

    override fun dismissProgressDialog() {
        progressbar.dismiss()
    }

    override fun showToast() {
        Toast.makeText(
            this, "Recognition failed.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun updateImage(image: Bitmap) {
        photo.setImageBitmap(image)
        imagePrev=image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter?.onActivityResult(requestCode, resultCode, data)
    }
    fun getImageUri(src: Bitmap, format: Bitmap.CompressFormat?, quality: Int): Uri? {
        val os = ByteArrayOutputStream()
        src.compress(format, quality, os)
        val path: String = MediaStore.Images.Media.insertImage(
            this.contentResolver,
            src,
            "Huawei-Lens",
            null
        )
        return Uri.parse(path)
    }

    companion object {
        lateinit var imagePrev: Bitmap
        lateinit var preview_uri: Uri

    }

}