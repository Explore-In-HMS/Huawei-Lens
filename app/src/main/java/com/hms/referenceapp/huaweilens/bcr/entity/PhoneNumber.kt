package com.hms.referenceapp.huaweilens.bcr.entity

class PhoneNumber (var type: String, var number: String) {

    companion object {
        const val TYPE_MOBILE = "M"
        const val TYPE_FAX = "F"
        const val TYPE_PHONE = "P"
    }
}