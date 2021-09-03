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

package com.hms.referenceapp.huaweilens.main.interfaces


import androidx.fragment.app.FragmentActivity
import com.hms.referenceapp.huaweilens.trt.camera.CameraSource
import com.hms.referenceapp.huaweilens.trt.camera.CameraSourcePreview
import com.hms.referenceapp.huaweilens.trt.views.GraphicOverlay


interface TrtFragmentInterface {

    interface CameraPresenter {
        fun initButtonCallbacks(cameraSource: CameraSource)
        fun initSpinner()
        fun closeSpinner()
        fun initOverlay(graphicOverlay: GraphicOverlay?)
        fun initPreview(preview: CameraSourcePreview?)

    }

    interface CameraView {
        fun getViewActivity() : FragmentActivity?
        fun createCameraSource()
        fun startCameraSource()
    }
}