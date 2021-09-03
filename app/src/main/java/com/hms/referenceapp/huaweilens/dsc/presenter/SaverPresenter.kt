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

package com.hms.referenceapp.huaweilens.dsc.presenter

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.dsc.contract.SaverActivityContract
import com.huawei.hms.mlsdk.text.MLText
import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class SaverPresenter(_view: SaverActivityContract.IView) : SaverActivityContract.IPresenter {

    private var view: SaverActivityContract.IView = _view
    private val rootPath = Constants.ROOT_FOLDER

    init {

        val folderMain = "Huawei-Lens"
        val f = File(Constants.ROOT_FOLDER, folderMain)
        if (!f.exists()) {
            f.mkdirs()
        }
    }

    override fun savefile(filename: String, content: String) {

        try {


            val file = File(rootPath, filename)
            file.setExecutable(true, false)
            val fos = FileOutputStream(file)
            fos.write(content.toByteArray())
            fos.flush()
            fos.close()

        } catch (e: Exception) {
            Log.d("222", e.toString())
        }
    }

    override fun savePdf(filename: String, content: MutableList<MLText.Block>) {

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(600, 1000, 1).create()
        val pageInfo2 = PdfDocument.PageInfo.Builder(600, 1000, 1).create()

        val page = document.startPage(pageInfo)

        lateinit var page2: PdfDocument.Page


        val paint = Paint()
        val x = 10
        var y = 25
        val z = 10
        var t = 25
        var counter = 0
        //40 satir sigiyor bir sayfaya  1000/25




        for (item: MLText.Block? in content) {

            if (item != null) {
                val block: String = item.stringValue


                val blocks: List<String> = block.split("\n")


                for (items: String in blocks) {


                    when {
                        counter == 40 -> {
                            page.canvas.drawText(items, x.toFloat(), y.toFloat(), paint)
                            document.finishPage(page)

                            counter++
                        }
                        counter > 40 -> {
                            if (counter == 41) {
                                page2 = document.startPage(pageInfo2)
                            }
                            page2.canvas.drawText(items, z.toFloat(), t.toFloat(), paint)
                            t += 25
                            counter++
                        }
                        else -> {
                            page.canvas.drawText(items, x.toFloat(), y.toFloat(), paint)
                            y += 25
                            counter++
                        }
                    }


                }


            }
        }


        if (counter > 40) {
            document.finishPage(page2)
        } else if (counter < 40) {
            document.finishPage(page)
        }

        val file = File(rootPath, filename)
        file.setExecutable(true, false)
        file.setExecutable(true, false)
        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: java.lang.Exception) {
            Log.d("222", e.toString())
        }

    }

    override fun saveImagePdf(bitmap: Bitmap, filename: String) {
        val document = PdfDocument()
        val pageInfo: PdfDocument.PageInfo = PdfDocument.PageInfo.Builder(
            bitmap.width,
            bitmap.height,
            1
        ).create()
        val page: PdfDocument.Page = document.startPage(pageInfo)


        val bitmapImg = bitmap.copy(Bitmap.Config.ARGB_8888, false)

        val canvas: Canvas = page.canvas
        val paint = Paint()

        val bitmap2 = Bitmap.createScaledBitmap(
            bitmapImg, bitmapImg.width, bitmapImg.height, true
        )

        canvas.drawBitmap(bitmap2, 0f, 0f, paint)

        document.finishPage(page)


        val file = File(rootPath, filename)
        file.setExecutable(true, false)

        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: java.lang.Exception) {
            Log.d("222", e.toString())
        }

    }

    override fun saveDocx(filename: String, content: MutableList<MLText.Block>) {
        val document = XWPFDocument()
        val paragraph: XWPFParagraph = document.createParagraph()
        val run: XWPFRun = paragraph.createRun()

        val file = File(rootPath, filename)
        file.setExecutable(true, false)
        file.setWritable(true)

        for (item: MLText.Block? in content) {

            if (item != null) {
                val block: String = item.stringValue
                val blocks: List<String> = block.split("\n")
                for (items: String in blocks) {

                    run.setText(items)
                    run.addBreak()

                }
            }
        }


        val out = FileOutputStream(file)
        document.write(out)
        out.close()


    }

    override fun saveDocxImage(bitmap: Bitmap, filenameImg: String, filename: String) {
        val f = File(view.getViewActivity().cacheDir, filenameImg)
        f.createNewFile()
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)

        val bitmapdata = bos.toByteArray()

        val fos = FileOutputStream(f)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()


        val document = XWPFDocument()
        val paragraph: XWPFParagraph = document.createParagraph()
        val run: XWPFRun = paragraph.createRun()
        paragraph.alignment = ParagraphAlignment.CENTER


        val file = File(rootPath, filename)
        file.setExecutable(true, false)
        file.setWritable(true)


        val iss = FileInputStream(f)
        run.addBreak()
        run.addPicture(
            iss,
            XWPFDocument.PICTURE_TYPE_PNG,
            filenameImg,
            Units.toEMU(bitmap.width.toDouble()),
            Units.toEMU(
                bitmap.height.toDouble()
            )
        )

        //create
        val out = FileOutputStream(file)
        document.write(out)
        out.close()
        f.delete()
    }

    override fun saveGallery(

        src: Bitmap,
        format: Bitmap.CompressFormat?,
        quality: Int,
        title: String
    ) {

        val os = ByteArrayOutputStream()
        src.compress(format, quality, os)
        MediaStore.Images.Media.insertImage(
            view.getViewActivity().contentResolver,
            src,
            title,
            null
        )
    }

}