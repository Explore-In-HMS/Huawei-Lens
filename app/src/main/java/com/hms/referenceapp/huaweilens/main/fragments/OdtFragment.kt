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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.interfaces.OdtFragmentInterface
import com.hms.referenceapp.huaweilens.main.presenters.OdtFragmentPresenter
import com.hms.referenceapp.huaweilens.odt.App
import com.hms.referenceapp.huaweilens.odt.activities.ClassificationActivity
import com.huawei.hms.mlsdk.common.MLApplication
import kotlinx.android.synthetic.main.fragment_odt.*


class OdtFragment : Fragment(), OdtFragmentInterface.CameraView {
    private lateinit var presenter: OdtFragmentInterface.CameraPresenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewCamera = inflater.inflate(R.layout.fragment_odt, container, false)
        MLApplication.getInstance().apiKey = App.API_KEY
        return viewCamera
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = OdtFragmentPresenter(this)
     //   presenter.checkPerms()
        presenter.initCamera()
        presenter.initButtonCallbacks()
    }

    override fun onPause() {
        super.onPause()
        tv_camera_torch?.text = requireContext().resources.getString(R.string.touch_here_to_turn_light_on)
        iv_camera_torch?.setImageDrawable(requireContext().getDrawable(R.drawable.ic_lightbulb))
        linearLayout3.alpha = 0.5f

    }

    companion object {
        const val title = "Object Translation"
    }

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {

                val bitmap = MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver,
                    result.data?.data
                )
                MenuActivity.SendImage.setBitmap(bitmap)
                val intent = Intent(requireActivity(), ClassificationActivity::class.java)
                startActivity(intent)
            }
        }

}