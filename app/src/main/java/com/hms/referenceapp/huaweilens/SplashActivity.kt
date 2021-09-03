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

package com.hms.referenceapp.huaweilens

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.hms.referenceapp.huaweilens.main.IntroductionActivity
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.PrefManager
import java.util.*


class SplashActivity : AppCompatActivity() {

    private lateinit var splashContainer: ConstraintLayout
    private lateinit var lensLogo: ImageView
    private lateinit var fromHuaweiLayout: LinearLayout
    private lateinit var lensText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        splashContainer = findViewById(R.id.splash_container)
        lensLogo = findViewById(R.id.splash_logo)
        fromHuaweiLayout = findViewById(R.id.fromHuaweiText)
        lensText = findViewById(R.id.splash_text)

        val logoAnimSet = AnimationSet(false)
        val logoScaleAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.scale)
        val logoRotateAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        val logoFadeOutAnim: Animation = AlphaAnimation(1f, 0f)
        logoScaleAnim.startOffset = 1000
        logoRotateAnim.startOffset = 1000
        logoFadeOutAnim.startOffset = 3400
        logoFadeOutAnim.duration = 500
        logoFadeOutAnim.fillAfter = true
        logoAnimSet.addAnimation(logoRotateAnim)
        logoAnimSet.addAnimation(logoScaleAnim)
        logoAnimSet.addAnimation(logoFadeOutAnim)

        val fromHuaweiLayoutFadeOutAnim: Animation = AlphaAnimation(1f, 0f)
        fromHuaweiLayoutFadeOutAnim.startOffset = 2500
        fromHuaweiLayoutFadeOutAnim.duration = 1000
        fromHuaweiLayoutFadeOutAnim.fillAfter = true

        val splashTextFadeOutAnim: Animation = AlphaAnimation(1f, 0f)
        splashTextFadeOutAnim.startOffset = 1500
        splashTextFadeOutAnim.duration = 500
        splashTextFadeOutAnim.fillAfter = true

        lensLogo.startAnimation(logoAnimSet)
        lensText.startAnimation(splashTextFadeOutAnim)
        fromHuaweiLayout.startAnimation(fromHuaweiLayoutFadeOutAnim)

        // Color Transition Animation
        val splashContainerAnim = ValueAnimator()
        val from: Int = ContextCompat.getColor(this, R.color.white)
        val to: Int = ContextCompat.getColor(this, R.color.black)
        splashContainerAnim.setIntValues(from, to)
        splashContainerAnim.setEvaluator(ArgbEvaluator())
        splashContainerAnim.addUpdateListener { valueAnimator ->
            splashContainer.setBackgroundColor(
                valueAnimator.animatedValue as Int
            )
        }
        splashContainerAnim.startDelay = 3000
        splashContainerAnim.duration = 1000
        splashContainerAnim.start()

        val prefManager = PrefManager(applicationContext)

        if (prefManager.isFirstTimeLaunch) {
            val intent = Intent(this, IntroductionActivity::class.java)
            val timer = Timer()
            val task = object : TimerTask() {
                override fun run() {
                    startActivity(intent)
                    finish()
                }
            }
            timer.schedule(task, 4000)
        } else {
            val intent = Intent(this, MenuActivity::class.java)
            val timer = Timer()
            val task = object : TimerTask() {
                override fun run() {
                    startActivity(intent)
                    finish()
                }
            }
            timer.schedule(task, 4000)
        }
    }
}