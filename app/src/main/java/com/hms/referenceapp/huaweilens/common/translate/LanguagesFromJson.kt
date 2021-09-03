package com.hms.referenceapp.huaweilens.common.translate

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class LanguagesFromJson(val context: Context) {

    private val assetFileName = "global_languages.json"

    private fun getJsonDataFromAsset(): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(assetFileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    fun get(): List<Language> {
        val jsonFileString = getJsonDataFromAsset()
        val gson = Gson()
        val listLanguageType = object : TypeToken<List<Language>>() {}.type
        return gson.fromJson(jsonFileString, listLanguageType)
    }

}