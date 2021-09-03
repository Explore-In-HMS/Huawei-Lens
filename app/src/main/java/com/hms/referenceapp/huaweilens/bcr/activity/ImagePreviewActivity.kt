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

@file:Suppress("DEPRECATION")

package com.hms.referenceapp.huaweilens.bcr.activity

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.bcr.entity.BusinessCardResult
import com.hms.referenceapp.huaweilens.bcr.processor.gcr.GeneralCardProcessor
import com.hms.referenceapp.huaweilens.bcr.processor.gcr.cardvisit.BusinessCardProcessor
import com.hms.referenceapp.huaweilens.bcr.util.BitmapUtils
import com.hms.referenceapp.huaweilens.dsc.cropper.CropImage
import com.hms.referenceapp.huaweilens.dsc.view.PreviewActivity
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.huawei.hms.mlplugin.card.gcr.MLGcrCapture
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureConfig
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureFactory
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureResult
import com.huawei.hms.mlsdk.text.MLText
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.IOException


class ImagePreviewActivity : AppCompatActivity() {

    lateinit var check: ImageView
    lateinit var ignore: ImageView
    lateinit var edit: ImageView
    private lateinit var photo: ImageView
    private var garbageUri: Uri? = null
    private val `object`: Any = false
    lateinit var progressDialog: ProgressDialog
    private lateinit var uri:Uri
    private lateinit var bitmap:Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.business_image_preview)

        photo = findViewById(R.id.imagepreview)
        check = findViewById(R.id.check)
        ignore = findViewById(R.id.ignore)
        edit = findViewById(R.id.edit)

        bitmap = MenuActivity.SendImage.getBitmap()!!
        photo.setImageBitmap(bitmap)
        //Picasso.get().load(uri).into(photo)

        check.setOnClickListener {

            progressDialog = ProgressDialog(it.context)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.setMessage("Results are loading, please wait")
            progressDialog.show()



            val options: HmsScanAnalyzerOptions =
                HmsScanAnalyzerOptions.Creator().setHmsScanTypes(
                    HmsScan.QRCODE_SCAN_TYPE,
                    HmsScan.DATAMATRIX_SCAN_TYPE
                )
                    .setPhotoMode(false).create()
            val hmsScans: Array<HmsScan> =
                ScanUtil.decodeWithBitmap(this, bitmap, options)
             // Process the decoding result when the scanning is successful.
             // Process the decoding result when the scanning is successful.
            if (hmsScans.isNotEmpty() && !TextUtils.isEmpty(hmsScans[0].getOriginalValue()) && hmsScans[0].scanTypeForm == HmsScan.CONTACT_DETAIL_FORM) {
               // Display the scanning result
                    val intent = Intent(Intent.ACTION_INSERT)
                    intent.type = ContactsContract.Contacts.CONTENT_TYPE
                    if(hmsScans[0].getContactDetail().getPeopleName() != null)
                        intent.putExtra(ContactsContract.Intents.Insert.NAME, hmsScans[0].getContactDetail().getPeopleName().fullName)
                    if(hmsScans[0].getContactDetail().getCompany() != null)
                        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, hmsScans[0].getContactDetail().getCompany())
                    if(hmsScans[0].getContactDetail().getTelPhoneNumbers().size != 0)
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, hmsScans[0].getContactDetail().getTelPhoneNumbers()[0].telPhoneNumber)
                    if(hmsScans[0].getContactDetail().getTelPhoneNumbers().size > 1)
                        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, hmsScans[0].getContactDetail().getTelPhoneNumbers()[1].telPhoneNumber)
                    if(hmsScans[0].getContactDetail().eMailContents.isNotEmpty())
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, hmsScans[0].getContactDetail().eMailContents[0].addressInfo)
                    if(hmsScans[0].getContactDetail().getAddressesInfos().size != 0)
                        intent.putExtra(ContactsContract.Intents.Insert.POSTAL, hmsScans[0].getContactDetail().getAddressesInfos()[0].addressDetails[0])
                   startActivity(intent)

            }
            else
            {
                val config = MLGcrCaptureConfig.Factory().create()
                val ocrManager = MLGcrCaptureFactory.getInstance().getGcrCapture(config)
                ocrManager.captureImage(bitmap, `object`, callback)

            }
            progressDialog.dismiss()

        }



        ignore.setOnClickListener {
            finish()
        }
        edit.setOnClickListener {
            progressDialog = ProgressDialog(it.context)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.setMessage("Crop is loading, please wait")
            progressDialog.show()
            uri = getImageUri(bitmap,Bitmap.CompressFormat.JPEG,100)!!
            CropImage.activity(uri)
                .start(this)
            progressDialog.dismiss()

        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if(data != null)
            {
            val result = CropImage.getActivityResult(data)
                val resultUri = result.uri

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    this.contentResolver?.let {
                        ImageDecoder.createSource(
                            it,
                            resultUri
                        )
                    }?.let {
                        ImageDecoder.decodeBitmap(
                            it
                        )
                    }
                } else {
                    MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        resultUri
                    )
                }

                Picasso.get().load(resultUri).into(photo)
                garbageUri = resultUri
                uri = resultUri
            }
        }
    }

    private val callback: MLGcrCapture.Callback = object : MLGcrCapture.Callback {
        override fun onResult(result: MLGcrCaptureResult, `object`: Any): Int {
            Log.i(TAG, "callback onRecSuccess")
            val idCard: GeneralCardProcessor?
            val businessCardResult: BusinessCardResult?
            idCard = BusinessCardProcessor(result.text)
            businessCardResult = idCard.result

            // If the results don't match
            return if (businessCardResult == null || businessCardResult.name.isEmpty() && (businessCardResult.phoneMobile.isEmpty() || businessCardResult.phoneNumberLand.isEmpty())) {
                Toast.makeText(
                    applicationContext,
                    "Not able to detect card please try again later!",
                    Toast.LENGTH_LONG
                ).show()
                progressDialog.dismiss()
                finish()
                MLGcrCaptureResult.CAPTURE_CONTINUE

            } else {
                progressDialog.dismiss()
                displayResult(businessCardResult)
                MLGcrCaptureResult.CAPTURE_STOP
            }
        }

        override fun onCanceled() {
            Log.i(TAG, "callback onRecCanceled")
        }

        override fun onFailure(i: Int, bitmap: Bitmap) {}
        override fun onDenied() {
            Log.i(TAG, "callback onCameraDenied")
        }
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


    private fun displayResult(result: BusinessCardResult?) {
        if (result == null) {
            return
        }
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = ContactsContract.Contacts.CONTENT_TYPE
        intent.putExtra(ContactsContract.Intents.Insert.NAME, result.name)
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, result.companyName)

        intent.putExtra(ContactsContract.Intents.Insert.PHONE, result.phoneNumberLand)
        intent.putExtra(
            ContactsContract.Intents.Insert.PHONE_TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME
        )

        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, result.phoneMobile)
        intent.putExtra(
            ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME
        )

        intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, result.phoneNumberFax)
        if (result.isPhoneNumberFax) intent.putExtra(
            ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK
        )

        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, result.mail)
        intent.putExtra(ContactsContract.Intents.Insert.POSTAL, result.address)

        val data = ArrayList<ContentValues>()
        val row1 = ContentValues()
        row1.put(
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
        )
        row1.put(ContactsContract.CommonDataKinds.Website.URL, result.website)
        data.add(row1)
        intent.putExtra(ContactsContract.Intents.Insert.DATA, data)

        //intent.putExtra(ContactsContract.Intents.Insert.DATA., result.website)

        startActivity(intent)
    }

    companion object {
        const val TAG = "BCRActivity"
    }

}