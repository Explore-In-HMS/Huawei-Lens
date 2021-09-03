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

package com.hms.referenceapp.huaweilens.main.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.Constants
import com.hms.referenceapp.huaweilens.tts.TTSInterface
import com.hms.referenceapp.huaweilens.tts.TTSPresenter
import com.huawei.hms.mlsdk.tts.MLTtsEngine
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.openxml4j.exceptions.OpenXML4JException
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.xmlbeans.XmlException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*


class TTSFragment : Fragment(), TTSInterface.TView, View.OnClickListener {

    private lateinit var presenter: TTSInterface.TPresenter
    private lateinit var myText: EditText
    private lateinit var ttsBtn: ImageButton
    private var engine: MLTtsEngine? = null
    private var extractor: XWPFWordExtractor? = null
    private lateinit var seekBarVolume: SeekBar
    private lateinit var seekBarSpeed: SeekBar
    private lateinit var pickerLayout: LinearLayout
    private lateinit var settingsLayout: LinearLayout
    private lateinit var settingsBtn: RadioButton
    private lateinit var pickerBtn: RadioButton
    private lateinit var clearBtn: Button
    private lateinit var infoBtn: Button
    private lateinit var chooseFile: Button
    private lateinit var maleButton: Button
    private lateinit var femaleButton: Button
    private var isMaleButton: Boolean = false
    private var isFemaleButton: Boolean = false
    private var isFromFile: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_t_t_s, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = TTSPresenter(this)
        ttsBtn = view.findViewById(R.id.buttonPlay)
        settingsBtn = view.findViewById(R.id.settings_button)
        pickerBtn = view.findViewById(R.id.pick_file_button)
        clearBtn = view.findViewById(R.id.clean)
        infoBtn = view.findViewById(R.id.info)
        chooseFile = view.findViewById(R.id.btn_choose_file)
        femaleButton = view.findViewById(R.id.btn_female)
        maleButton = view.findViewById(R.id.btn_male)
        myText = view.findViewById(R.id.tts_text)
        myText.setTextIsSelectable(true)
        myText.movementMethod = ScrollingMovementMethod()
        ttsBtn.setOnClickListener(this as View.OnClickListener)
        chooseFile.setOnClickListener(this as View.OnClickListener)
        clearBtn.setOnClickListener(this as View.OnClickListener)
        infoBtn.setOnClickListener(this as View.OnClickListener)
        seekBarVolume = view.findViewById(R.id.seek_bar_volume)
        seekBarSpeed = view.findViewById(R.id.seek_bar_speed)
        pickerLayout = view.findViewById(R.id.picker_button_layout)
        settingsLayout = view.findViewById(R.id.settings_layout)
        activity?.let {
            ActivityCompat.requestPermissions(
                it, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), PackageManager.PERMISSION_GRANTED
            )
        }


        pickerBtn.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.transcription_type_radio_button)
                settingsLayout.setBackgroundResource(0)
                pickerLayout.visibility = View.VISIBLE
                settingsLayout.visibility = View.GONE
            }
        }

        settingsBtn.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.setBackgroundResource(R.drawable.transcription_type_radio_button)
                pickerLayout.setBackgroundResource(0)
                settingsLayout.visibility = View.VISIBLE
                pickerLayout.visibility = View.GONE
            }

        }

        myText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    //just need to check onTextChanged
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (myText.hasFocus()) {
                        isFromFile = false
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    //just need to check onTextChanged
                }
            }
        )

    }


    @Throws(Exception::class)
    fun readDocument(uri: Uri?) {
        myText.setText("")
        var fullPath: String? = null
        if (uri!!.path!!.contains(Constants.PRIMARY)) {
            fullPath = Constants.LOCAL_STORAGE + uri.path!!.split(
                Constants.COLON
            ).toTypedArray()[1]
        }
        if (uri.path!!.contains("home") && uri.path!!.contains("document")) {
            fullPath = Constants.EXT_STORAGE + "Documents/" + uri.path!!.split(
                Constants.COLON
            ).toTypedArray()[1]
        }
        if (fullPath != null && fullPath.contains(".pdf")) {
            Log.v("URI", uri.path + " " + fullPath)
            val stringParser: String
            val pdf = File(fullPath)
            try {
                val pdfReader = PdfReader(pdf.path)
                stringParser = PdfTextExtractor.getTextFromPage(pdfReader, 1).trim { it <= ' ' }
                pdfReader.close()
                isFromFile = true
                presenter.giveText(stringParser)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Log.v("URI", uri.path + " " + fullPath)
            val word = File(fullPath!!)
            if (uri.path!!.split(Constants.COLON).toTypedArray()[1].toLowerCase(Locale.ROOT)
                    .endsWith(
                        ".docx"
                    )
            ) {
                val doc = XWPFDocument(FileInputStream(word))
                extractor = XWPFWordExtractor(doc)
                val extractedText = extractor!!.text
                isFromFile = true
                presenter.giveText(extractedText)
            } else {
                val document = HWPFDocument(FileInputStream(word))
                val range = document.range
                val len = range.numParagraphs()
                val builder = StringBuilder()
                for (i in 0 until len) {
                    builder.append(range.getParagraph(i).text())
                    myText.setText(builder.toString())
                }
            }
        }
    }

    private val fileSelectorForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            processSelectedFile(result)
        }

    private fun openFile() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            val mimeTypes = arrayOf(
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            )
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            fileSelectorForResult.launch(
                Intent.createChooser(
                    intent, requireContext().resources.getString(
                        R.string.import_file_message
                    )
                )
            )
        } catch (ex: ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(
                activity, requireContext().resources.getString(R.string.error_install_file_manager),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun processSelectedFile(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val uri = data.data
                Log.d("PATH", "onActivityResult: " + uri!!.path)
                try {
                    readDocument(uri)
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: OpenXML4JException) {
                    e.printStackTrace()
                } catch (e: XmlException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun useEngine(mlTtsEngine: MLTtsEngine?) {
        engine = mlTtsEngine
    }

    override fun ttsButton(): ImageButton? {
        return ttsBtn
    }

    override fun isMale(xy: Boolean): Boolean? {
        isMaleButton = xy
        return isMaleButton
    }

    override fun isFemale(xx: Boolean): Boolean? {
        isFemaleButton = xx
        return isFemaleButton
    }

    override fun fromFile(): Boolean {
        return isFromFile
    }

    override fun maleButtonP(): Button? {
        return this.maleButton
    }

    override fun femaleButtonP(): Button? {
        return this.femaleButton
    }

    override fun sourceText(): String? {
        return myText.text.toString()
    }

    override fun makeItClickable(btn: Button) {
        btn.isClickable = true
        if (btn == maleButton) {
            btn.setBackgroundResource(R.drawable.click_button_male)
        } else if (btn == femaleButton) {
            btn.isClickable = true
            btn.setBackgroundResource(R.drawable.click_female_button)
        }
    }

    override fun listen(str: String) {
        activity?.runOnUiThread {
            val newStr = str.trim { it <= ' ' }.replace("\n".toRegex(), "")
            val spannableString = SpannableString(myText.text)
            val backgroundSpans = spannableString.getSpans(
                0,
                spannableString.length,
                BackgroundColorSpan::class.java
            )
            for (span: BackgroundColorSpan? in backgroundSpans) {
                spannableString.removeSpan(span)
            }
            var index = spannableString.toString().indexOf(newStr)
            if (index >= 0) {
                spannableString.setSpan(
                    BackgroundColorSpan(Color.BLACK),
                    index,
                    index + newStr.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                index = spannableString.toString().indexOf(newStr, index + newStr.length)
            }
            val m = myText.selectionStart
            myText.setSelection(m)
            myText.setText(spannableString)
        }
    }

    override fun selectSpeaker(lng: String) {
        when (lng) {
            "en" -> {
                setEnglishSpeaker(lng)
            }
            "zh" -> {
                setChineseSpeaker(lng)
            }
            "de" -> {
                setDefaultSpeaker(lng)
            }
            "es" -> {
                setDefaultSpeaker(lng)
            }
            "it" -> {
                setDefaultSpeaker(lng)
            }
            "fr" -> {
                setDefaultSpeaker(lng)
            }
            "other" -> {
                setOtherLngSpeaker()
            }
        }
    }

    private fun setDefaultSpeaker(lng: String) {
        presenter.setConfigs(lng, "female")
        maleButton.visibility = View.VISIBLE
        femaleButton.visibility = View.VISIBLE
        maleButton.isClickable = false
        femaleButton.isClickable = false
        isFemaleButton
        femaleButton.setBackgroundResource(R.drawable.buttonfemale)
        maleButton.setBackgroundResource(R.drawable.buttondisable)
    }

    private fun setEnglishSpeaker(lng: String) {
        maleButton.visibility = View.VISIBLE
        femaleButton.visibility = View.VISIBLE
        if (presenter.giveCurrentLng() != null) {
            maleButton.isClickable = false
            femaleButton.isClickable = false
            if (isMaleButton) {
                presenter.setConfigs(lng, "male")
                maleButton.setBackgroundResource(R.drawable.buttonmale)
                femaleButton.setBackgroundResource(R.drawable.buttondisable)
            } else if (isFemaleButton) {
                presenter.setConfigs(lng, "female")
                maleButton.setBackgroundResource(R.drawable.buttondisable)
                femaleButton.setBackgroundResource(R.drawable.buttonfemale)
            }
        } else {
            showDialog(requireContext().resources.getString(R.string.your_speaker), lng)
        }
    }

    private fun setChineseSpeaker(lng: String) {
        maleButton.visibility = View.VISIBLE
        femaleButton.visibility = View.VISIBLE
        if (presenter.giveCurrentLng() != null) {
            maleButton.isClickable = false
            femaleButton.isClickable = false
            if (isMaleButton) {
                presenter.setConfigs(lng, "male")
                maleButton.setBackgroundResource(R.drawable.buttonmale)
                femaleButton.setBackgroundResource(R.drawable.buttondisable)
            } else if (isFemaleButton) {
                presenter.setConfigs(lng, "female")
                maleButton.setBackgroundResource(R.drawable.buttondisable)
                femaleButton.setBackgroundResource(R.drawable.buttonfemale)
            }
        } else {
            showDialog(
                requireContext().resources.getString(R.string.your_speaker_chinese),
                lng
            )
        }
    }

    private fun setOtherLngSpeaker() {
        maleButton.visibility = View.VISIBLE
        femaleButton.visibility = View.VISIBLE
        if (presenter.giveCurrentLng() != null) {
            maleButton.isClickable = false
            femaleButton.isClickable = false
            if (isMaleButton) {
                presenter.setConfigs("en", "male")
                maleButton.setBackgroundResource(R.drawable.buttonmale)
                femaleButton.setBackgroundResource(R.drawable.buttondisable)
            } else if (isFemaleButton) {
                presenter.setConfigs("en", "female")
                maleButton.setBackgroundResource(R.drawable.buttondisable)
                femaleButton.setBackgroundResource(R.drawable.buttonfemale)
            }
        } else {
            showDialog(requireContext().resources.getString(R.string.select_speaker), "en")
        }
    }

    override fun setVolume(): Float {
        var volume: Float = seekBarVolume.progress / 50.0f
        if (volume < 0.1) volume = 0.1f
        return volume
    }

    override fun setSpeed(): Float {
        var speed: Float = seekBarSpeed.progress / 50.0f
        if (speed < 0.1) speed = 0.1f
        return speed
    }

    override fun setMyText(str: String) {
        myText.setText(str)
        myText.setCompoundDrawables(null, null, null, null)
        isFromFile = true
    }

    private fun showDialog(title: String, lng: String) {
        val dialog = activity?.let { Dialog(it) }
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog)
        val body = dialog.findViewById(R.id.tvBody) as TextView
        body.text = title
        val maleBtn = dialog.findViewById(R.id.btn_male) as FloatingActionButton
        val femaleBtn = dialog.findViewById(R.id.btn_female) as FloatingActionButton
        maleBtn.setOnClickListener {
            if (lng == "en") {
                presenter.setConfigs(lng, "male")
            } else if (lng == "zh") {
                presenter.setConfigs(lng, "male")
            }
            isMale(true)
            maleButton.setBackgroundResource(R.drawable.buttonmale)
            isFemale(false)
            femaleButton.setBackgroundResource(R.drawable.buttondisable)
            dialog.dismiss()
        }
        femaleBtn.setOnClickListener {
            if (lng == "en") {
                presenter.setConfigs(lng, "female")
            } else if (lng == "zh") {
                presenter.setConfigs(lng, "female")
            }
            isFemale(true)
            femaleButton.setBackgroundResource(R.drawable.buttonfemale)
            isMale(false)
            maleButton.setBackgroundResource(R.drawable.buttondisable)
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonPlay -> {
                if (isFromFile) {
                    presenter.trigger(myText.text.toString())
                } else {
                    presenter.giveText(myText.text.toString())
                }
            }
            R.id.btn_choose_file -> {
                if (engine != null) {
                    engine!!.stop()
                    openFile()
                } else {
                    openFile()
                }
            }
            R.id.clean -> {
                if (engine != null) {
                    engine!!.stop()
                    myText.setText("")
                    val drawable = context?.getDrawable(R.drawable.ic_text)
                    myText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                } else {
                    myText.setText("")
                    val drawable = context?.getDrawable(R.drawable.ic_text)
                    myText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                }
            }
            R.id.info -> {
                val infoDialog = activity?.let { Dialog(it) }
                infoDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                infoDialog.setCancelable(false)
                infoDialog.setContentView(R.layout.info_dialog)
                infoDialog.show()
                val okBtn = infoDialog.findViewById(R.id.ok) as FloatingActionButton
                okBtn.setOnClickListener {
                    infoDialog.dismiss()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (engine != null) {
            engine!!.stop()
        }
    }
}