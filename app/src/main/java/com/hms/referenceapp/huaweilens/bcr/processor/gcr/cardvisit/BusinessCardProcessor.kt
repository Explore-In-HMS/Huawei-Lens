/**
 * Copyright 2020. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hms.referenceapp.huaweilens.bcr.processor.gcr.cardvisit

import android.graphics.Rect
import android.util.Log
import android.util.Patterns
import com.hms.referenceapp.huaweilens.bcr.processor.gcr.GeneralCardProcessor
import com.hms.referenceapp.huaweilens.bcr.entity.BlockItem
import com.hms.referenceapp.huaweilens.bcr.entity.BusinessCardResult
import com.hms.referenceapp.huaweilens.bcr.entity.PhoneNumber
import com.hms.referenceapp.huaweilens.bcr.util.StringUtils.extractEmail
import com.hms.referenceapp.huaweilens.bcr.util.StringUtils.unaccentString
import com.huawei.hms.mlsdk.text.MLText
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Post processing plug-in of Hong Kong, Macao and Taiwan pass recognition
 *
 * @since 2020-03-12
 */
class BusinessCardProcessor(private val text: MLText) : GeneralCardProcessor {
    override val result: BusinessCardResult?
        get() {
            val blocks = text.blocks
            if (blocks.isEmpty()) {
                Log.i(TAG, "Result blocks is empty")
                return null
            }
            val originItems: ArrayList<BlockItem> = getOriginItems(blocks)
            var companyName = ""
            var mail = ""
            var phoneLand = ""
            var phoneNumberFax = ""
            var isPhoneNumberFax = false
            var phoneMobile = ""
            var name = ""
            var title = ""
            var addressLine1 = ""
            var addressLine2 = ""
            var webSite = ""
            var blockItemRemoveMail = BlockItem("", Rect())
            var blockItemRemoveAddressLine1 = BlockItem("", Rect())
            var blockItemRemoveAddressLine2 = BlockItem("", Rect())
            var blockItemRemoveWebsite = BlockItem("", Rect())

            //    var mailFlag = false
            val phoneList: MutableList<PhoneNumber> = mutableListOf()
            val phoneListLineNumbers: MutableList<Int> = mutableListOf()

            var regexEscape = "[\\s.-]+";

            originItems.forEachIndexed { t, item ->
                val tempStr: String = item.text
                val re =
                    "(([Tt](\\D){0,10})|([Pp](\\D){0,10})|([Mm](\\D){0,7})|([Gg](\\D){0,7})|([Ff](\\D){0,5})|([Cc](\\D){0,5}))?((((\\+|00)(\\d{1,3}))|(\\(0\\))|(0))?([\\s.-]?[(]?\\d{3,4}?[)]?[\\s.-]?\\d{3}[\\s.-]?\\d{2}[\\s.-]?\\d{2}))".toRegex()

                val results = re.findAll(tempStr)

                results.forEachIndexed { i, result ->

                    // add line number
                    phoneListLineNumbers.add(t)

                    // remove result from the line
                    val fullMatch = result.value
                    originItems[t].text = originItems[t].text.replace(fullMatch, "")

                    // determine phone number type
                    var type: String = PhoneNumber.TYPE_PHONE //default value

                    if (result.groups[1] != null) {
                        result.groups[1].let {
                            val v = it!!.value.toLowerCase(Locale.ROOT)
                            type = if (v.contains("m") || v.contains("c") || v.contains("g")) {
                                PhoneNumber.TYPE_MOBILE
                            } else if (v.contains("f")) {
                                PhoneNumber.TYPE_FAX
                            } else {
                                PhoneNumber.TYPE_PHONE
                            }
                        }
                    }

                    // determine phone number
                    var number = ""
                    if (result.groups[14] != null) {
                        number = result.groups[14]!!.value.replace(regexEscape.toRegex(), "")
                    }

                    phoneList.add(
                        PhoneNumber(type, number)
                    )
                }
            }

            val faxNumbers = phoneList.filter { it.type == "F" }
            val mobileNumbers = phoneList.filter { it.type == "M" }
            val phoneNumbers = phoneList.filter { it.type == "P" }

            val sizeA = phoneNumbers.size

            phoneNumbers.forEachIndexed { i, p ->
                if (i == 0) phoneLand = p.number
                if (i == 1) phoneMobile = p.number
                if (i == 2) phoneNumberFax = p.number
            }

            mobileNumbers.forEachIndexed { i, p ->
                if (i == 0) phoneMobile = p.number
                if (i == 1 && sizeA == 0) phoneLand = p.number
                if (i == 2 && sizeA == 0) phoneNumberFax = p.number
            }

            faxNumbers.forEachIndexed { i, p ->
                isPhoneNumberFax = true
                if (i == 0) phoneNumberFax = p.number
            }

//            phoneListLineNumbers.distinct().forEach {
//                //originItems.removeAt(it)
//            }

            for (item in originItems) {
                val tempStr: String = item.text
                val nsfw = listOf(
                    "SK",
                    "MH",
                    "SOK",
                    "MAH",
                    "STREET",
                    "ST",
                    "CD",
                    "CAD",
                    "ADDRESS",
                    "OFFICE",
                    "FLOOR",
                    "ROAD",
                    "PLAZA",
                    "CADDESI",
                    "MAHALLESI",
                    "SOKAGI"
                )

                val rx = Regex("\\b(?:${nsfw.joinToString(separator = "|")})\\b")
                if (rx.containsMatchIn(tempStr.toUpperCase(Locale.getDefault()))) {
                    addressLine1 += tempStr
                    blockItemRemoveAddressLine1 = item
                }
            }
            originItems.remove(blockItemRemoveAddressLine1)

            for (item in originItems) {
                val tempStr: String = item.text
                val re = Regex("[^0-9]")
                val numberString = re.replace(tempStr, "")
                if (numberString.length in 4..5 || tempStr.contains("/") || tempStr.toUpperCase(
                        Locale.getDefault()
                    ).contains("NO")
                ) {
                    addressLine2 += tempStr
                    blockItemRemoveAddressLine2 = item
                }
            }
            originItems.remove(blockItemRemoveAddressLine2)

            for (item in originItems) {
                val tempStr: String = item.text
                val result = extractEmail(tempStr)
                if (result.isNotEmpty()) {
                    mail = result
                    blockItemRemoveMail = item
                }
            }
            originItems.remove(blockItemRemoveMail)

            var indexName: Int? = null
            val mailName = mail.split("@")[0].replace(regexEscape.toRegex(), "")
                .toLowerCase(Locale.ROOT)
            originItems.forEachIndexed lit@{ i, item ->
                val tempStr: String = unaccentString(item.text).toLowerCase(Locale.ROOT)
                val split = tempStr.split(" ")
                split.forEach {
                    if (it.length >= 3 && mailName.contains(it)) {
                        name = item.text
                        indexName = i
                        if (originItems.lastIndex > i) {
                            title = processTitle(originItems[i + 1])
                        }
                       return@lit
                    }
                }

            }

            if (indexName != null  && name.isNotEmpty()) {
                originItems.removeAt(indexName!!)
            }



            for (item in originItems) {
                val tempStr: String = item.text
                val regex = Patterns.WEB_URL.toRegex()
                val result = regex.find(tempStr)
                if (result != null) {
                    webSite = result.value
                    blockItemRemoveWebsite = item
                }
            }
            originItems.remove(blockItemRemoveWebsite)

            if (mail.isNotEmpty()) {
                for (item in originItems) {
                    val tempStr: String = unaccentString(item.text).replace("[-]+".toRegex(), "")
                        .toLowerCase(Locale.ROOT)

                    val firmName =
                        mail.split("@")[1].split(".")[0].replace(regexEscape.toRegex(), "")
                            .toLowerCase(Locale.ROOT)

                    val split = tempStr.split(" ")
                    split.forEach {
                        if (it.length >= 3 && (firmName.contains(it) || it.contains(firmName))) {
                            companyName = item.text
                        }
                    }
                    if (companyName.isNotEmpty()) {
                        originItems.remove(item)
                        break
                    }
                }
            }
            if (webSite.isNotEmpty()) {
                val websiteString = webSite.replace("[\\s.-]+".toRegex(), "")
                    .toLowerCase(Locale.ROOT)
                for (item in originItems) {
                    val tempStr: String = unaccentString(item.text).toLowerCase(Locale.ROOT)
                    val split = tempStr.split(" ")
                    split.forEach {
                        if (it.isNotEmpty() && websiteString.contains(it)) {
                            companyName = item.text
                            originItems.remove(item)
                        }
                    }
                    if (companyName.isNotEmpty()) {
                        originItems.remove(item)
                        break
                    }
                }
            }

            if (companyName.isNotEmpty() && name.isEmpty()) {
                name = companyName
                companyName = ""
            }



            return BusinessCardResult(
                mail,
                phoneMobile,
                phoneLand,
                phoneNumberFax,
                isPhoneNumberFax,
                "$addressLine1 $addressLine2",
                "$name $title",
                companyName,
                webSite
            )
        }

    private fun processTitle(blockItem: BlockItem): String {

        val titlePattern = "^(([A-z-.]+|[^\\x00-\\x7F]+){3,}(\\s)?){1,3}\$".toRegex()
        val result = titlePattern.find(blockItem.text)

        return if (result != null && result.value.isNotEmpty()) {
            blockItem.text
        } else {
            ""
        }
    }

    private fun getOriginItems(blocks: List<MLText.Block>): ArrayList<BlockItem> {
        val originItems: ArrayList<BlockItem> = ArrayList()
        for (block in blocks) {
            // Add in behavior units
            val lines = block.contents
            for (line in lines) {
                val points = line.vertexes
                val rect = Rect(points[0].x, points[0].y, points[2].x, points[2].y)
                val item = BlockItem(line.stringValue, rect)
                originItems.add(item)
            }
        }
        return originItems
    }

    companion object {
        private const val TAG = "PassCardProcessor"
    }
}