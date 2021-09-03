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

package com.hms.referenceapp.huaweilens.bcr

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.bcr.activity.ImagePreviewActivity
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.fragments.FrFragment
import com.hms.referenceapp.huaweilens.main.interfaces.BcrFragmentInterface
import com.hms.referenceapp.huaweilens.main.presenters.BcrFragmentPresenter
import com.huawei.hms.mlsdk.common.MLApplication
import kotlinx.android.synthetic.main.fragment_business_card.iv_camera_torch
import kotlinx.android.synthetic.main.fragment_business_card.linearLayout3
import kotlinx.android.synthetic.main.fragment_business_card.tv_camera_torch


class BusinessCardRecognition : Fragment() {
    private lateinit var presenter: BcrFragmentInterface.BcrPresenter
    private lateinit var cl: ConstraintLayout
    private lateinit var inflater:LayoutInflater
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewCamera = inflater.inflate(R.layout.fragment_business_card, container, false)
        MLApplication.getInstance().apiKey =
            "CgB6e3x9IdI9DYYI/IY9T4SBMt5hX0pzZjznmMu4hJ7xmGE/N15RR7VHfFwBi4zA6Vyx58vIhS1XGty1ZU2JpR6H"
        return viewCamera
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = BcrFragmentPresenter(this)
        presenter.initCamera()
        presenter.initButtonCallbacks()
        cl=view.findViewById(R.id.linearLayout3)
    }

     fun showProgressDialog() {

        inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        FrFragment.viewInflate =inflater.inflate(R.layout.rotated_progress_dialog,null)
        cl.alpha= 1F
        cl.addView(FrFragment.viewInflate)


    }


     fun dismissProgressDialog() {
        cl.alpha= 0.5F
        cl.removeView(FrFragment.viewInflate)

    }

    fun startGallery() {
        val cameraIntent = Intent(Intent.ACTION_PICK)
        cameraIntent.type = "image/*"
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            myActivityResultLauncher.launch(cameraIntent)
        }
    }


    private var myActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { it ->
            if (it.resultCode == RESULT_OK) {
               val returnUri: Uri? = it.data?.data
               val intent = Intent(requireActivity(), ImagePreviewActivity::class.java)

                if (returnUri != null) {
                    MenuActivity.SendImage.setBitmap(MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver,
                        returnUri
                    ))
                    requireActivity().startActivity(intent)
                }
            }
        }
    ) as ActivityResultLauncher<Intent>

    override fun onDestroy() {
        super.onDestroy()
        presenter.unRegisterSensor()
    }

    override fun onStop() {
        super.onStop()
        presenter.unRegisterSensor()
    }

    override fun onStart() {
        super.onStart()
        presenter.registerSensor()
    }

    override fun onPause() {
        super.onPause()
        presenter.unRegisterSensor()
        tv_camera_torch?.text = requireContext().resources.getString(R.string.touch_here_to_turn_light_on)
        iv_camera_torch?.setImageDrawable(requireContext().getDrawable(R.drawable.ic_lightbulb))
        linearLayout3.alpha = 0.5f

    }

    override fun onResume() {
        super.onResume()
        presenter.registerSensor()
    }

    companion object {
        const val title = "Business Card"
    }
}