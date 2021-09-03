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

package com.hms.referenceapp.huaweilens.common

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.utils.DocumentShare
import com.hms.referenceapp.huaweilens.main.MenuActivity
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class FileActivity : AppCompatActivity() {


    companion object {
        var lstFileModel: List<FileModel>? = null
        lateinit var myrv: RecyclerView
        lateinit var mContext: Context
        lateinit var myAdapter: RecyclerViewAdapter
        lateinit var layoutNoFilesFound: ConstraintLayout
        lateinit var files: List<File>
        var fileSize: Int? = null
        var sameName: Boolean? = null
        var mymenu: Menu? = null


        fun updateView(position: Int) {

            (lstFileModel as ArrayList<FileModel>).removeAt(position)

            (myrv.adapter as RecyclerViewAdapter).notifyItemRemoved(position)

            (myrv.adapter as RecyclerViewAdapter).notifyItemRangeChanged(
                position,
                (lstFileModel as ArrayList<FileModel>).size
            )

            if ((lstFileModel as ArrayList<FileModel>).size == 0) {
                layoutNoFilesFound.visibility = View.VISIBLE

            }

        }


        fun share(file: File) {
            val shareIntent =
                DocumentShare(
                    mContext,
                    file.toUri()
                )
            shareIntent.share()
        }

        fun renameFile(position: Int, mfile: File, extention: String) {
            val mInflater = LayoutInflater.from(mContext)
            val view: View = mInflater.inflate(R.layout.dsc_alert_dialog_rename, null)
            val alertDialog: AlertDialog = AlertDialog.Builder(mContext).create()
            alertDialog.setTitle("  Huawei-Lens")
            alertDialog.setIcon(R.mipmap.huaweilogo)
            alertDialog.setCancelable(false)
            val etComments = view.findViewById<View>(R.id.etComments) as EditText

            alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE, "OK"
            ) { _, _ ->

                val text: String = etComments.text.toString()
                val newName = "$text.$extention"
                for (path: File in files) {

                    val filename: String = path.name.substring(path.name.lastIndexOf("/") + 1)
                    if (filename == newName) {
                        sameName = true
                        Toast.makeText(
                            mContext,
                            "There is a file with the same name.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                if (sameName != true) {
                    val te = File(Constants.ROOT_FOLDER, newName)
                    val renameCtrl = mfile.renameTo(te)
                    if (!renameCtrl) {
                        Toast.makeText(mContext, "Rename action failed.", Toast.LENGTH_LONG).show()
                    } else if (renameCtrl) {

                        (lstFileModel as ArrayList<FileModel>)[position].title = newName
                        (lstFileModel as ArrayList<FileModel>)[position].file = te
                        (myrv.adapter as RecyclerViewAdapter).notifyItemChanged(position)
                        (myrv.adapter as RecyclerViewAdapter).notifyDataSetChanged()
                        files[position].renameTo(te)
                        sameName = false
                    }
                }
            }

            alertDialog.setButton(
                AlertDialog.BUTTON_NEGATIVE, "CANCEL"
            ) { _, _ ->
                alertDialog.dismiss()
            }
            alertDialog.setView(view)
            alertDialog.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (MenuActivity.SendImage.ctrl == 0) {
                    onBackPressed()
                } else {
                    val intent = Intent(this, MenuActivity::class.java)
                    startActivity(intent)
                }

                true
            }
            R.id.fa_select_all -> {
                selectAllAndUndoSelect(1,true)
                RecyclerViewAdapter.viewCounter = lstFileModel!!.size

                true
            }
            R.id.fa_undoSelection -> {
                selectAllAndUndoSelect(2,false)
                true
            }
            R.id.fa_delete -> {
                val alertDialog: AlertDialog = AlertDialog.Builder(mContext).create()
                alertDialog.setTitle("  Huawei-Lens")
                alertDialog.setIcon(R.mipmap.huaweilogo)
                alertDialog.setCancelable(false)
                alertDialog.setMessage("Are you sure you want to delete the file(s) permanently ?")
                alertDialog.setButton(
                    AlertDialog.BUTTON_POSITIVE, "OK"
                ) { _, _ ->
                    RecyclerViewAdapter.updateController = 3

                    val tempList: ArrayList<FileModel>? = arrayListOf()
                    var counter = 0
                    for (item in (lstFileModel as ArrayList<FileModel>)) {
                        if (item.selected == true) {
                            tempList?.add(item)
                            item.file.delete()
                            counter++
                        }
                    }
                    (lstFileModel as ArrayList<FileModel>).removeAll(tempList as ArrayList<FileModel>)
                    (myrv.adapter as RecyclerViewAdapter).notifyDataSetChanged()

                    if ((lstFileModel as ArrayList<FileModel>).size == 0) {
                        layoutNoFilesFound.visibility = View.VISIBLE
                        mymenu?.setGroupVisible(R.id.tool_items, false)
                    }

                }
                alertDialog.setButton(
                    AlertDialog.BUTTON_NEGATIVE, "CANCEL"
                ) { _, _ ->
                    alertDialog.dismiss()
                    RecyclerViewAdapter.updateController = 3
                    RecyclerViewAdapter.viewCounter = 0
                    (myrv.adapter as RecyclerViewAdapter).notifyDataSetChanged()

                }
                alertDialog.show()
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun selectAllAndUndoSelect(updateController:Int,isSelected:Boolean) {
        RecyclerViewAdapter.updateController = updateController
        for (item in lstFileModel!!) {
            item.selected = isSelected
        }
        (myrv.adapter as RecyclerViewAdapter).notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.file_activity_toolbar, menu)
        for (i in 0 until menu!!.size()) {
            val item = menu.getItem(i)
            val spanString = SpannableString(menu.getItem(i).title.toString())
            val end = spanString.length
            spanString.setSpan(RelativeSizeSpan(0.8f), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spanString.setSpan(
                ForegroundColorSpan(Color.WHITE),
                0,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            item.title = spanString
        }
        mymenu = menu
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycler_files)
        layoutNoFilesFound = findViewById(R.id.layout_no_files_found)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true);
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val rootPath = Environment.getExternalStorageDirectory().absolutePath + "/Huawei-Lens/"
        files = getListFiles(File(rootPath))

        mContext = this@FileActivity
        sameName = false

        lstFileModel = ArrayList()

        val wordIcon = BitmapFactory.decodeResource(
            this.resources,
            R.mipmap.ic_word_foreground
        )

        val pdfIcon = BitmapFactory.decodeResource(
            this.resources,
            R.mipmap.ic_pdf_foreground
        )
        val txtIcon = BitmapFactory.decodeResource(
            this.resources,
            R.mipmap.ic_txt_foreground
        )

        val excelIcon = BitmapFactory.decodeResource(
            this.resources,
            R.mipmap.ic_excel_foreground
        )

        fileSize = files.size

        if (fileSize!! > 0) {
            for (path: File in files) {
                when (path.extension) {
                    "pdf" -> {
                        (lstFileModel as ArrayList<FileModel>).add(
                            FileModel(
                                path.name,
                                path.extension,
                                false,
                                pdfIcon,
                                path
                            )
                        )
                    }
                    "docx" -> {
                        (lstFileModel as ArrayList<FileModel>).add(
                            FileModel(
                                path.name,
                                path.extension,
                                false,
                                wordIcon,
                                path
                            )
                        )
                    }
                    "txt" -> {
                        (lstFileModel as ArrayList<FileModel>).add(
                            FileModel(
                                path.name,
                                path.extension,
                                false,
                                txtIcon,
                                path
                            )
                        )

                    }
                    "xlsx" -> {
                        (lstFileModel as ArrayList<FileModel>).add(
                            FileModel(
                                path.name,
                                path.extension,
                                false,
                                excelIcon,
                                path
                            )
                        )

                    }
                }
            }
        } else {
            layoutNoFilesFound.visibility = View.VISIBLE

        }
        myrv = findViewById<View>(R.id.recyclerview_id) as RecyclerView
        myAdapter = RecyclerViewAdapter(this, lstFileModel as ArrayList<FileModel>)
        myrv.layoutManager = GridLayoutManager(this, 2)
        myrv.adapter = myAdapter


    }


    private fun getListFiles(parentDir: File): List<File> {


        val direct = File(Constants.ROOT_FOLDER)
        if (!direct.exists()) {
            direct.mkdir()
        }
        val inFiles = ArrayList<File>()
        val files = parentDir.listFiles()
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                if (file.isDirectory) {
                    inFiles.addAll(getListFiles(file))
                } else {
                    if (file.name.endsWith(".pdf") || file.name.endsWith(".docx") || file.name.endsWith(
                            ".txt"
                        ) || file.name.endsWith(".xlsx")
                    ) {
                        inFiles.add(file)
                    }
                }


            }
            inFiles.sortWith(compareBy { it.lastModified() })
            inFiles.reverse()
        }
        return inFiles
    }


}