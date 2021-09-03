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

package com.hms.referenceapp.huaweilens.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("CommitPrefEdits")
class SharedPrefs(context: Context) {
    private val mPreferences: SharedPreferences
    private val mEditor: SharedPreferences.Editor
    fun putStringValue(key: String?, value: String?) {
        mEditor.putString(key, value)
        mEditor.commit()
    }

    fun getStringValue(key: String?): String? {
        return mPreferences.getString(key, null)
    }

    fun putIntValue(key: String?, value: Int) {
        mEditor.putInt(key, value)
        mEditor.commit()
    }

    fun getIntValue(key: String?): Int {
        return mPreferences.getInt(key, -1)
    }

    companion object {
        const val TAG = "SharedPrefs"
        private var mSharedPreferencesUtil: SharedPrefs? = null

        @JvmStatic
        fun getInstance(context: Context): SharedPrefs? {
            if (mSharedPreferencesUtil == null) {
                synchronized(SharedPrefs::class.java) {
                    if (mSharedPreferencesUtil == null) {
                        mSharedPreferencesUtil =
                            SharedPrefs(
                                context
                            )
                    }
                }
            }
            return mSharedPreferencesUtil
        }
    }

    init {
        mPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        mEditor = mPreferences.edit()
    }
}