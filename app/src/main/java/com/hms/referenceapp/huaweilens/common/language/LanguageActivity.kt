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

package com.hms.referenceapp.huaweilens.common.language

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.dialog.AftLanguageNotsupported
import com.hms.referenceapp.huaweilens.audio.dialog.NoInternetConnectionError
import com.hms.referenceapp.huaweilens.common.translate.GetLanguageArray
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel
import kotlinx.android.synthetic.main.activity_language.*
import java.text.NumberFormat


private val languages: MutableList<Language> = mutableListOf()
private var mLanguageArray: GetLanguageArray? = null
private var progressNumberFormat: String = "Downloaded %1d MB of %2d MB "


data class Language(val title: String, val isExist: Boolean, val isoCode: String)

var manager: MLLocalModelManager = MLLocalModelManager.getInstance()


class LanguageActivity : AppCompatActivity() {
    private var mContext=this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        val downloadStrategy = MLModelDownloadStrategy.Factory()
            .needWifi() // It is recommended that you download the package in a Wi-Fi environment.
            .create()

        val progressDialog = ProgressDialog(this)


        languages.sortByDescending { it.isExist }


        val mAdapter = ListAdapter(languages)
        list_recycler_view.setHasFixedSize(true)
        list_recycler_view.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        mLanguageArray = GetLanguageArray(GetLanguageArray.LOCAL, this, false)
        for (i in mLanguageArray!!.getNames().indices) {
            if (mLanguageArray!!.getIsoCodes()[i] != "zh" || mLanguageArray!!.getIsoCodes()[i] != "en") {

                manager.isModelExist(
                    MLLocalTranslatorModel.Factory(
                        mLanguageArray!!.getIsoCodes()[i].toString()
                    ).create()
                ).addOnSuccessListener {

                    if (it) {
                        languages.add(
                            Language(
                                mLanguageArray!!.getNames()[i].toString(), true,
                                mLanguageArray!!.getIsoCodes()[i].toString()
                            )
                        )
                        languages.sortByDescending { it.isExist }

                        mAdapter.notifyDataSetChanged()

                    } else if (!it) {
                        languages.add(
                            Language(
                                mLanguageArray!!.getNames()[i].toString(), false,
                                mLanguageArray!!.getIsoCodes()[i].toString()
                            )
                        )
                        languages.sortByDescending { it.isExist }

                        mAdapter.notifyDataSetChanged()

                    }

                }

            }
        }


        list_recycler_view.adapter = mAdapter

        val langCodeSpinner: String? = intent.getStringExtra("LanguageCode")
        val sourceLangCodeSpinner: String? = intent.getStringExtra("sourceLanguageCode")

        val pDialog = ProgressDialog(this@LanguageActivity);
        val translateLang =
            mLanguageArray!!.getLanguages().find { it.iso6391 == langCodeSpinner }?.name
        val sourceLang =
            mLanguageArray!!.getLanguages().find { it.iso6391 == sourceLangCodeSpinner }?.name


        val modelDownloadListenerFromFragment =
            MLModelDownloadListener { alreadyDownLength, totalLength ->
                progressDialog.dismiss()

                runOnUiThread {
                    pDialog.setTitle(getString(R.string.please_wait))
                    pDialog.setMessage(
                        getString(
                            R.string.download_language_pack_downloaded,
                            translateLang
                        )
                    );
                    pDialog.isIndeterminate = false;
                    pDialog.progress = (alreadyDownLength / 1000000).toInt()
                    pDialog.setProgressNumberFormat(progressNumberFormat)
                    pDialog.setProgressPercentFormat(NumberFormat.getPercentInstance())
                    pDialog.max = (totalLength / 1000000).toInt();
                    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pDialog.setCancelable(false);
                    pDialog.show();
                }
            }

        val modelDownloadListenerFromFragmentSource =
            MLModelDownloadListener { alreadyDownLength, totalLength ->
                progressDialog.dismiss()

                runOnUiThread {
                    pDialog.setTitle(getString(R.string.please_wait))
                    pDialog.setMessage(
                        getString(
                            R.string.download_language_pack_downloaded,
                            sourceLang
                        )
                    );
                    pDialog.isIndeterminate = false;
                    pDialog.progress = (alreadyDownLength / 1000000).toInt()
                    pDialog.setProgressNumberFormat(progressNumberFormat)
                    pDialog.setProgressPercentFormat(NumberFormat.getPercentInstance())
                    pDialog.max = (totalLength / 1000000).toInt();
                    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pDialog.setCancelable(false);
                    pDialog.show();
                }
            }

        if(hasNetworkAvailable(mContext)){
        if (langCodeSpinner != null) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.setMessage("Model is downloading, please wait")
            progressDialog.show()
            manager.downloadModel(
                MLLocalTranslatorModel.Factory(
                    langCodeSpinner
                ).create(), downloadStrategy, modelDownloadListenerFromFragment
            ).addOnSuccessListener {
                if (sourceLangCodeSpinner != null) {
                    manager.downloadModel(
                        MLLocalTranslatorModel.Factory(
                            sourceLangCodeSpinner
                        ).create(), downloadStrategy, modelDownloadListenerFromFragmentSource
                    ).addOnSuccessListener {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        onBackPressed()
                    }
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    onBackPressed()
                }
            }.addOnFailureListener {
                progressDialog.dismiss()
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        } else if (sourceLangCodeSpinner != null) {

            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.setMessage("Model is downloading, please wait")
            progressDialog.show()
            manager.downloadModel(
                MLLocalTranslatorModel.Factory(
                    sourceLangCodeSpinner
                ).create(), downloadStrategy, modelDownloadListenerFromFragmentSource
            ).addOnSuccessListener {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                onBackPressed()
            }.addOnFailureListener {
                progressDialog.dismiss()
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }
        else{
            callDialog()
        }



        mAdapter.setOnItemClickListener(object : ListAdapter.ClickListener {
            override fun onItemClick(v: View, position: Int) {
                if (!mAdapter.getItem(position).isoCode.equals("en"))
                {

                if (mAdapter.getItem(position).isExist) {
                    val builder = AlertDialog.Builder(this@LanguageActivity)
                    builder.setTitle(mAdapter.getItem(position).title)
                    builder.setMessage(getString(R.string.delete_language_pack))
                    builder.setPositiveButton("YES", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            manager.deleteModel(
                                MLLocalTranslatorModel.Factory(
                                    mAdapter.getItem(position).isoCode
                                ).create()
                            ).addOnSuccessListener {

                                Toast.makeText(
                                    applicationContext,
                                    getString(
                                        R.string.toast_deleted,
                                        mAdapter.getItem(position).title
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                                onBackPressed()
                            }.addOnFailureListener {
                            }
                        }

                    }).setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            p0!!.dismiss()
                        }

                    })
                    builder.show()

                } else {
                    if(hasNetworkAvailable(mContext)){
                    val builder = AlertDialog.Builder(this@LanguageActivity)
                    builder.setTitle(mAdapter.getItem(position).title)
                    builder.setMessage(getString(R.string.download_language_pack_request))
                    builder.setPositiveButton("YES", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {

                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            )

                            val itemLang = mLanguageArray!!.getLanguages()
                                .find { it.iso6391 == mAdapter.getItem(position).isoCode }?.name
                            val pDialog = ProgressDialog(this@LanguageActivity);

                            val modelDownloadListener =
                                MLModelDownloadListener { alreadyDownLength, totalLength ->
                                    progressDialog.dismiss()

                                    runOnUiThread {
                                        pDialog.setTitle(getString(R.string.please_wait))
                                        pDialog.setMessage(
                                            getString(
                                                R.string.download_language_pack_downloaded,
                                                itemLang
                                            )
                                        );
                                        pDialog.isIndeterminate = false;
                                        pDialog.progress = (alreadyDownLength / 1000000).toInt()
                                        pDialog.setProgressNumberFormat(progressNumberFormat)
                                        pDialog.setProgressPercentFormat(NumberFormat.getPercentInstance())
                                        pDialog.max = (totalLength / 1000000).toInt();
                                        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                        pDialog.setCancelable(false);
                                        pDialog.show();
                                    }
                                }

                            progressDialog.setCanceledOnTouchOutside(false)
                            progressDialog.setMessage("Model is downloading, please wait")
                            progressDialog.show()

                            manager.downloadModel(
                                MLLocalTranslatorModel.Factory(
                                    mAdapter.getItem(position).isoCode
                                ).create(), downloadStrategy, modelDownloadListener
                            ).addOnSuccessListener {

                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                onBackPressed()

                            }.addOnFailureListener {
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        }

                    }).setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            p0!!.dismiss()
                        }

                    })
                    builder.show()
                }
                    else{
                        callDialog()
                    }
                }


            }

            }

        })
        imagebutton_back.setOnClickListener {
            onBackPressed()
        }

    }

    private fun callDialog() {
       val myDialog = NoInternetConnectionError.build(this).create()
       myDialog.show()
    }

    private fun hasNetworkAvailable(context: Context): Boolean {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager?
        val network = manager?.activeNetworkInfo
        return (network != null)
    }

    override fun onBackPressed() {
        languages.clear()
        super.onBackPressed()
    }
}