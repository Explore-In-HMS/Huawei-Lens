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

package com.hms.referenceapp.huaweilens.dsc.view

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hms.referenceapp.huaweilens.R
import com.squareup.picasso.Picasso
import java.io.File
import kotlin.properties.Delegates

class SkewPreview : AppCompatActivity() {

    lateinit var dsc_image: ImageView
    lateinit var dsc_edit: ImageView
    lateinit var dsc_next: ImageView
    lateinit var dsc_cancel: ImageView
    lateinit var skew_status: TextView


    companion object {
        lateinit var dsc_uri: Uri
        var dsc_status by Delegates.notNull<Boolean>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.skew_preview)

        dsc_image = findViewById(R.id.dsc_skewpreview)
        dsc_next = findViewById(R.id.dsc_next)
        dsc_cancel = findViewById(R.id.dsc_cancel)
        dsc_edit = findViewById(R.id.dsc_edit)
        skew_status = findViewById(R.id.CorrectionStatus)




        Picasso.get().load(dsc_uri).into(dsc_image)



        if (dsc_status == true) {
            skew_status.text = "Skew Correction is Successful"
            skew_status.setBackgroundColor(Color.GREEN)
        } else {
            skew_status.text = "Skew Correction is Failed"
            skew_status.setBackgroundColor(Color.RED)
        }



        dsc_next.setOnClickListener {

            val intent = Intent(this, SaverActivity::class.java)
            startActivity(intent)


        }

        dsc_cancel.setOnClickListener {
           onBackPressed()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}