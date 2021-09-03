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


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.bcr.activity.ImagePreviewActivity
import com.hms.referenceapp.huaweilens.dsc.view.PreviewActivity
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.interfaces.DscInterface
import com.hms.referenceapp.huaweilens.main.presenters.DscFragmentPresenter
import com.huawei.hms.mlsdk.common.MLApplication
import kotlinx.android.synthetic.main.dsc_fragment.*

class DscFragment : Fragment(),DscInterface.CameraView {

    private lateinit var presenter: DscInterface.CameraPresenter
    lateinit var progressbar: ProgressDialog



    override fun onCreateView(



        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewCamera = inflater.inflate(R.layout.dsc_fragment, container, false)
        MLApplication.getInstance().apiKey =
            "CgB6e3x9IdI9DYYI/IY9T4SBMt5hX0pzZjznmMu4hJ7xmGE/N15RR7VHfFwBi4zA6Vyx58vIhS1XGty1ZU2JpR6H"
        return viewCamera
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = DscFragmentPresenter(this)
        presenter.checkPerms()
        presenter.initCamera()
        presenter.initButtonCallbacks()
        progressbar = ProgressDialog(activity)
        progressbar.setCanceledOnTouchOutside(false)
        progressbar.setMessage("Results are loading, please wait")


    }



    override fun getViewActivity(): FragmentActivity? {
      return  activity
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onPause() {
        super.onPause()
        tv_camera_torch?.text = requireContext().resources.getString(R.string.touch_here_to_turn_light_on)
        iv_camera_torchh?.setImageDrawable(requireContext().getDrawable(R.drawable.ic_lightbulb))
        linearLayout3.alpha = 0.5f
    }


   private var myActivityResultLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult<Intent, ActivityResult>(
    ActivityResultContracts.StartActivityForResult(),
    ActivityResultCallback<ActivityResult> { it ->
        if (it.resultCode == Activity.RESULT_OK) {
            val returnUri: Uri? = it.data?.data
            val intent = Intent(activity, PreviewActivity::class.java)

            if (returnUri != null) {
              //  PreviewActivity.uri = returnUri


              val bitmap:Bitmap= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    activity?.contentResolver?.let {
                        ImageDecoder.createSource(
                            it,
                            returnUri
                        )
                    }?.let {
                        ImageDecoder.decodeBitmap(
                            it
                        )
                    }!!
                } else {
                    MediaStore.Images.Media.getBitmap(
                        activity?.contentResolver,
                        returnUri
                    )
                }

                MenuActivity.SendImage.setBitmap(bitmap)
                activity?.startActivity(intent)
            }
        }
    }
    ) as ActivityResultLauncher<Intent>


    override fun startActivityFResult(intent: Intent, requestCode: Int) {
        startActivityForResult(intent, requestCode)
    }



    override fun showProgressDialog() {
        progressbar.show()
    }

    override fun dismissProgressDialog() {
        progressbar.dismiss()
    }

    override fun showGallery() {
        val cameraIntent = Intent(Intent.ACTION_PICK)
        cameraIntent.type = "image/*"
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            myActivityResultLauncher.launch(cameraIntent)
        }
    }


}