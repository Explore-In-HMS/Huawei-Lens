package com.hms.referenceapp.huaweilens.common.translate

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.utils.AudioTranscriptionUtils
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager
import com.huawei.hms.mlsdk.translate.MLTranslateLanguage
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel
import kotlinx.android.synthetic.main.custom_language_spinner_row.view.*
import kotlin.properties.Delegates


data class Language (var iso6391: String, var name: String, var native: String, var isDownloaded: Boolean = false)

class GetLanguageArray(
    private var translatorType: String,
    var context: Context,
    private var showOriginalLabel: Boolean
) {

    private var languageCodes: Set<String>? = null
    private var languages: MutableList<Language> = mutableListOf()
    private var shift by Delegates.notNull<Int>()
    private var array: Array<String?> = arrayOf()
    private var manager: MLLocalModelManager = MLLocalModelManager.getInstance()
    var modelCheckListener: (()->Unit)? = null
    var modelCheckListenerForSharedPref: (()->Unit)? = null
    private val originalItem = Language("", context.resources.getString(R.string.str_original), context.resources.getString(R.string.str_original),true)

    init {
        this.shift = if(showOriginalLabel) 1 else 0

        when(translatorType) {
        CLOUD -> try {
                languageCodes = MLTranslateLanguage.syncGetCloudAllLanguages()
            } catch (e: MLException) {
                AudioTranscriptionUtils.showToast(context as Activity, context.resources.getString(R.string.error_cloud_translation_unavailable))
            }
        LOCAL -> try {
                languageCodes = MLTranslateLanguage.syncGetLocalAllLanguages()
            } catch (e: MLException) {
                AudioTranscriptionUtils.showToast(context as Activity, context.resources.getString(R.string.error_local_translation_unavailable))
            }
        else -> languageCodes = null
        }

        if(!languageCodes.isNullOrEmpty()) {

            languages = LanguagesFromJson(context)
                .get().filter { language ->
                    languageCodes!!.contains(language.iso6391)
                }
                .sortedBy { it.name }.toMutableList()

            ////////////////////////////////////////
            // Remove unsupported languages on-device translation
            /*if(translatorType == LOCAL) {
                val removeLanguages = arrayOf("cs", "he", "hi", "id", "el", "ro", "sr", "tl", "hu", "ta", "nl",
                "fa", "sk", "et", "lv", "km")
                removeLanguages.forEach {
                    languages.remove(
                        languages.find { s-> s.iso6391 == it }
                    )
                }
            }*/
            ////////////////////////////////////////

        }

        if(showOriginalLabel) {
            array = arrayOfNulls(languages.size + this.shift)
            array[0] = context.resources.getString(R.string.str_original)
        }
        else {
            array = arrayOfNulls(languages.size)
        }
    }


    @SuppressLint("DefaultLocale")
    fun getNames(): Array<String?> {
        languages.forEachIndexed { index, it ->
            array[index+shift] = it.name.capitalize()
        }
        return array
    }

    @SuppressLint("DefaultLocale")
    fun getNativeNames(): Array<String?> {
        languages.forEachIndexed { index, it ->
            array[index+shift] = it.native.capitalize()
        }
        return array
    }

    fun getIsoCodes(): Array<String?> {
        languages.forEachIndexed { index, it ->
            array[index+shift] = it.iso6391
        }
        return array
    }

    fun getLanguages(): MutableList<Language> {
        return languages
    }


    fun sortByDownloadedModels(): GetLanguageArray {
        if(translatorType == LOCAL) {
            var c = 0
            languages.forEach { language ->
                manager.isModelExist(
                    MLLocalTranslatorModel.Factory(language.iso6391).create()
                ).addOnSuccessListener { isDownloaded ->
                    c++
                    language.isDownloaded = isDownloaded

                    ///////////// set english downloaded as default ////////////////
                    if(language.iso6391 == "en") language.isDownloaded = true
                    ///////////////////////////////////////////////////////////////

                    if (c == languages.size) {

                        val byDownloaded = Comparator.comparing { m: Language -> !m.isDownloaded }
                        val byName = Comparator.comparing { m: Language -> m.name }
                        languages.sortWith(byDownloaded.thenComparing(byName))

                        if(showOriginalLabel) languages = (mutableListOf(originalItem) + languages).toMutableList()
                        modelCheckListener?.invoke()
                        modelCheckListenerForSharedPref?.invoke()
                    }
                }
            }
        }
        return this
    }

    companion object {
        const val LOCAL = "local"
        const val CLOUD = "cloud"
    }

}