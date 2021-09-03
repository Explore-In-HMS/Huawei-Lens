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

package com.hms.referenceapp.huaweilens.odt.activities

import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.main.interfaces.ClassificationInterface
import com.hms.referenceapp.huaweilens.main.presenters.ClassificationPresenter
import com.hms.referenceapp.huaweilens.odt.App
import com.huawei.hms.mlsdk.common.MLApplication
import kotlinx.android.synthetic.main.activity_classification.*


class ClassificationActivity : AppCompatActivity() {

    private lateinit var presenter: ClassificationInterface.ClassificatonPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classification)
        recyclerView.layoutManager = LinearLayoutManager(
            App.context,
            LinearLayoutManager.VERTICAL,
            false
        )
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        MLApplication.getInstance().apiKey = App.API_KEY
        presenter = ClassificationPresenter(this)
    //    presenter.downloadModels()

    }

    override fun onResume() {
        super.onResume()
        presenter.downloadModels()
    }
}