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

package com.hms.referenceapp.huaweilens.fr.presenter


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.FileActivity
import com.hms.referenceapp.huaweilens.dsc.cropper.CropImage
import com.hms.referenceapp.huaweilens.fr.contract.FrPreviewContract
import com.hms.referenceapp.huaweilens.fr.model.ResponseJson
import com.hms.referenceapp.huaweilens.fr.view.FrPreview
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzerFactory
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzerSetting
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FrPreviewPresenter(_view: FrPreviewContract.View) : FrPreviewContract.Presenter {

    private var view: FrPreviewContract.View = _view
    val tableElementList: MutableList<String> = ArrayList()
    val coordinates: MutableList<Int> = ArrayList()
    val rootPath = Environment.getExternalStorageDirectory().absolutePath + "/Huawei-Lens/"
    var excelFile: File? = null
    var row: Row? = null
    var isEmpty:Boolean = true


    override fun initFormRecognition(image: Bitmap?) {
        val setting = MLFormRecognitionAnalyzerSetting.Factory().create()
        val analyzer = MLFormRecognitionAnalyzerFactory.getInstance().getFormRecognitionAnalyzer(
            setting
        )

        val mlFrame = MLFrame.fromBitmap(image)

        val recognizeTask = analyzer.asyncAnalyseFrame(mlFrame)
        recognizeTask.addOnSuccessListener {

            val mJsonString: String = it.toString()
            val parser = JsonParser()
            val mJson = parser.parse(mJsonString)
            val gson = Gson()
            val response = gson.fromJson(mJson, ResponseJson::class.java)



            if (response.retCode==-1){
                view.dismissProgressDialog()
                showDialog()
            }else{
                for (i in response.tableContent?.tables?.get(0)?.tableBody?.indices!!) {

                    val tableInfo = response.tableContent!!.tables!![0].tableBody!![i].textInfo
                    val coordinateInfo = response.tableContent!!.tables!![0].tableBody!![i].startRow
                    if (tableInfo != null && coordinateInfo != null) {
                        tableElementList.add(tableInfo)
                        coordinates.add(coordinateInfo)
                    }
                    if (tableInfo != null && tableInfo.trim().isNotEmpty()) {
                       isEmpty=false
                    }

                }
                if (tableElementList.size > 0 && !isEmpty ) {
                    createExcelFile(tableElementList)
                } else {
                    showDialog()
                }


                if (analyzer != null) {
                    try {
                        analyzer.stop()
                    } catch (ioException: IOException) {
                        Log.d("22", "analyzer error")
                    }
                }
            }



        }.addOnFailureListener {
            view.dismissProgressDialog()
            showDialog()


            if (analyzer != null) {
                try {
                    analyzer.stop()
                } catch (ioException: IOException) {
                    Log.d("22333", "analyzer error")
                }
            }
        }


    }

    private fun showDialog() {

       val mInflater = LayoutInflater.from(view.getViewActivity())
      val vieww: View = mInflater.inflate(R.layout.fr_info_dialog, null)

        val alertDialog: AlertDialog = AlertDialog.Builder(view.getViewActivity()).create()

        alertDialog.setTitle("  Huawei Lens")
        alertDialog.setIcon(R.mipmap.huaweilogo)
        alertDialog.setCancelable(false)


        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "OK"
        ) { _, _ ->

            alertDialog.dismiss()
            view.dismissProgressDialog()
        }

        alertDialog.setView(vieww)
        alertDialog.show()



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE &&
            CropImage.getActivityResult(data) != null) {


                val result = CropImage.getActivityResult(data)
                val resultUri = result.uri

                var bitmap:Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    view.getViewActivity().contentResolver?.let {
                        ImageDecoder.createSource(
                            it,
                            resultUri
                        )
                    }?.let {
                        ImageDecoder.decodeBitmap(
                            it
                        )
                    }!!
                } else {
                    MediaStore.Images.Media.getBitmap(
                        view.getViewActivity().contentResolver,
                        resultUri
                    )
                }

                view.updateImage(bitmap)
                FrPreview.preview_uri=resultUri


                if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(view.getViewActivity(), "Crop failed.", Toast.LENGTH_LONG).show()
                }
            }

    }

    override fun createExcelFile(elements: MutableList<String>) {

        val fileName = SimpleDateFormat("yyyy-dd-M-HH-mm-ss'.xlsx'").format(Date())

        var rowNum = 0
        var colNum = 1
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Huawei-Lens")
        var rowController = true


        for (i in elements.indices) {

            if (i >= coordinates.size - 1) {

                val cell = row?.createCell(colNum)
                if (cell != null) {
                    cell.setCellValue(tableElementList[i])
                }
                writeExcelFile(fileName, workbook)
                break
            }

            if (rowController) {
                rowNum++
                colNum = 1
                row = sheet.createRow(rowNum)
                rowController = false
            }
            if (coordinates[i + 1] > coordinates[i]) {
                rowController = true
            }
            val cell = row?.createCell(colNum)
            if (cell != null) {
                cell.setCellValue(tableElementList[i])
            }
            colNum++

        }
    }

    fun writeExcelFile(fileName: String, workbook: XSSFWorkbook) {
        try {

            excelFile = File(rootPath, fileName)
            excelFile!!.setExecutable(true, false)
            val outputStream = FileOutputStream(excelFile)

            workbook.write(outputStream)
            workbook.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        view.dismissProgressDialog()
        MenuActivity.SendImage.ctrl=2
        val intent = Intent(view.getViewActivity(), FileActivity::class.java)
        view.getViewActivity().startActivity(intent)

    }
    fun rotateBitmap(bitmap: Bitmap): Bitmap{
        val matrix = Matrix()

        matrix.postRotate(270F)

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

        val rotatedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )

        return rotatedBitmap
    }
}