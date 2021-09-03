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

package com.hms.referenceapp.huaweilens.audio.presenter

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.entity.ResultSentence
import com.hms.referenceapp.huaweilens.audio.utils.AudioTranscriptionUtils
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.common.utils.SharedPrefs
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor


class SaverPresenter(_view: SaverActivityContract.View) : SaverActivityContract.Presenter {

    private var view: SaverActivityContract.View = _view
    private val now = Date()
    private val dateFormatDocument = SimpleDateFormat("MM/dd/yyyy HH:mm:ss z", Locale.CANADA)
    private val dateFormatFile = SimpleDateFormat("yyyyMMddHHmmss", Locale.CANADA)
    private var fileUri: Uri? = null
    private var fileName = "Transcription_"
    private var recognizedSentences: MutableList<ResultSentence>
    private var translatedSentences: MutableList<ResultSentence>
    private val sharedPrefs: SharedPrefs =
        SharedPrefs(view.getViewActivity())
    var showTranslation = false

    private val strFileHeader = view.getViewActivity().resources.getString(R.string.file_transcription_result).toUpperCase(
        Locale.ROOT)
    private val strCreatedTime = view.getViewActivity().resources.getString(R.string.created_time) + " " + dateFormatDocument.format(now)


    init {
        val folderMain = "Huawei-Lens"
        val f = File(Constants.ROOT_FOLDER, folderMain)
        if (!f.exists()) {
            f.mkdirs()
        }
        val gson = Gson()
        val mutableListTypeResultSentence =
            object : TypeToken<MutableList<ResultSentence>>() {}.type

        recognizedSentences = gson.fromJson(
            sharedPrefs.getStringValue("recognized_sentences"),
            mutableListTypeResultSentence
        )

        translatedSentences = gson.fromJson(
            sharedPrefs.getStringValue("translated_sentences"),
            mutableListTypeResultSentence
        )

    }

    override fun saveTxt() {
        try {
            val fileNameWithExtension = "$fileName.txt"
            val file = File(Constants.ROOT_FOLDER, fileNameWithExtension)
            file.setExecutable(true, false)
            val fos = FileOutputStream(file)

            val content = StringBuilder()
            content.append(strFileHeader)
            content.append("\n")
            content.append(strCreatedTime)
            content.append("\n")

            if (!recognizedSentences.isNullOrEmpty()) {
                val duration = recognizedSentences.last().endTime
                recognizedSentences.forEachIndexed { i, it ->
                    val startTimeText =
                        AudioTranscriptionUtils.strActiveTime(it.startTime.toInt(), duration.toInt())
                    val endTimeText =
                        AudioTranscriptionUtils.strActiveTime(it.endTime.toInt(), duration.toInt())
                    content.append("[$startTimeText - $endTimeText]")
                    content.append("\n")
                    content.append(it.text)

                    // add translated text
                    if(showTranslation) {
                        translatedSentences[i].text.let { t->
                            if(t != it.text) {
                                content.append("\n")
                                content.append(t)
                            }
                        }
                    }
                    content.append("\n\n")
                }
            }

            fos.write(content.toString().toByteArray())
            fos.flush()
            fos.close()

        } catch (e: Exception) {
            Log.d("222", e.toString())
        }
    }

    override fun savePdf() {
        val fileNameWithExtension = "$fileName.pdf"
        val document = PdfDocument()
        val paint = Paint()
        val pw = 600 //page width
        val ph = 960 //page height
        val x = 30f //page margin from left
        val y = 30f //page margin from top
        val lh = 25f //line height
        val linePerPage = floor((ph - y - y) / lh) //36
        val maxCharLine = 100 // maximum number of characters on each line
        var lineCounter = 0
        val pageInfoList: MutableList<PdfDocument.PageInfo> = mutableListOf(PdfDocument.PageInfo.Builder(pw, ph, 1).create())
        var currentPage = document.startPage(pageInfoList[0])

        currentPage.canvas.drawText(strFileHeader, x, y + (lh * lineCounter), paint) // first line of first page

        lineCounter++
        currentPage.canvas.drawText(strCreatedTime, x, y + (lh * lineCounter), paint) // second line of first page
        lineCounter++


        if (!recognizedSentences.isNullOrEmpty()) {
            val duration = recognizedSentences.last().endTime
            val blockLines = if(showTranslation) 4f else 3f //num of lines of each block - each line is listed below

            recognizedSentences.forEachIndexed { i, it ->
                val startTimeText =
                    AudioTranscriptionUtils.strActiveTime(it.startTime.toInt(), duration.toInt())
                val endTimeText =
                    AudioTranscriptionUtils.strActiveTime(it.endTime.toInt(), duration.toInt())

                // finish the page and add new one (run once)
                val isPageOverFlown = ceil((lineCounter + blockLines) / linePerPage) > 1
                if(isPageOverFlown) {
                    document.finishPage(currentPage)
                    pageInfoList.add(PdfDocument.PageInfo.Builder(pw, ph, pageInfoList.lastIndex + 1).create())
                    currentPage = document.startPage(pageInfoList.last())
                    lineCounter = -1
                }

                // 1st line of the block
                lineCounter++
                currentPage.canvas.drawText("[$startTimeText - $endTimeText]", x, y + (lh * lineCounter), paint)

                // 2nd line of the block
                val chunkedOriginalText = it.text.chunked(maxCharLine)
                chunkedOriginalText.forEach { text ->
                    lineCounter++
                    currentPage.canvas.drawText(text, x, y + (lh * lineCounter), paint)
                }

                // 3rd line of the block - translation text (optional)
                if(showTranslation) {
                    translatedSentences[i].text.let { t->
                        if(t != it.text) {
                            val chunkedTranslationText = t.chunked(maxCharLine)
                            chunkedTranslationText.forEach { text ->
                                lineCounter++
                                currentPage.canvas.drawText(text, x, y + (lh * lineCounter), paint)
                            }
                        }
                    }
                }

                // 3rd or 4th line of the block  - space
                lineCounter++

            }
        }

        document.finishPage(currentPage)
        val file = File(Constants.ROOT_FOLDER, fileNameWithExtension)
        file.setExecutable(true, false)
        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: java.lang.Exception) {
            Log.d("222", e.toString())
        }

    }

    override fun saveDoc() {

        val fileNameWithExtension = "$fileName.docx"
        val document = XWPFDocument()
        val paragraph: XWPFParagraph = document.createParagraph()
        val run: XWPFRun = paragraph.createRun()
        val file = File(Constants.ROOT_FOLDER, fileNameWithExtension)
        this.fileUri = FileProvider.getUriForFile(
            view.getViewActivity() as Context,
            view.getViewActivity().resources.getString(R.string.package_name),
            file
        )

        file.setExecutable(true, false)
        file.setWritable(true)
        run.setText(strFileHeader)
        run.addBreak()
        run.setText(strCreatedTime)
        run.addBreak()
        run.addBreak()

        if (!recognizedSentences.isNullOrEmpty()) {
            val duration = recognizedSentences.last().endTime
            recognizedSentences.forEachIndexed { i, it ->
                val startTimeText =
                    AudioTranscriptionUtils.strActiveTime(it.startTime.toInt(), duration.toInt())
                val endTimeText =
                    AudioTranscriptionUtils.strActiveTime(it.endTime.toInt(), duration.toInt())
                run.setText("[$startTimeText - $endTimeText]")
                run.addBreak()
                run.setText(it.text)
                // add translated text
                if(showTranslation) {
                    translatedSentences[i].text.let { t->
                        if(t != it.text) {
                            run.addBreak()
                            run.setText(t)
                        }
                    }
                }
                run.addBreak()
                run.addBreak()
            }
        }
        val out = FileOutputStream(file)
        document.write(out)
        out.close()
    }

    override fun bindFileName(originalFileName: String) {
        AudioTranscriptionUtils.obtainFileNameWithOutExtension(originalFileName).let {
            fileName += if (!it.isNullOrBlank()) {
                it
            } else {
                dateFormatFile.format(now)
            }
        }
    }

}