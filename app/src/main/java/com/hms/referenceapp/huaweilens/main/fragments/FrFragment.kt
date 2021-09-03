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
import android.content.Context
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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.fr.view.FrPreview
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.interfaces.FrInterface
import com.hms.referenceapp.huaweilens.main.presenters.FrPresenter
import com.huawei.hms.mlsdk.common.MLApplication
import kotlinx.android.synthetic.main.fr_fragment.*

class FrFragment :Fragment(),FrInterface.CameraView {

    private lateinit var presenter: FrInterface.CameraPresenter
    lateinit var progressbar: ProgressDialog
    private lateinit var animation: Animation
    private lateinit var rotate: ImageView
    private lateinit var cl:ConstraintLayout
    private lateinit var inflater:LayoutInflater
    private  var ctrl:Boolean=true



    override fun onCreateView(


        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewCamera = inflater.inflate(R.layout.fr_fragment, container, false)
        MLApplication.getInstance().apiKey =
            "CgB6e3x9IdI9DYYI/IY9T4SBMt5hX0pzZjznmMu4hJ7xmGE/N15RR7VHfFwBi4zA6Vyx58vIhS1XGty1ZU2JpR6H"
        return viewCamera
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = FrPresenter(this)
        presenter.checkPerms()
        presenter.initCamera()
        presenter.initButtonCallbacks()
        rotate= view.findViewById(R.id.fr_rotate_left)
         cl=view.findViewById(R.id.fr_linearLayout3)
        ctrl=true
        animation= AnimationUtils.loadAnimation(activity, R.anim.anim_blink)
         rotate.startAnimation(animation)
         presenter.initSensor()



    }
    private var myActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { it ->
                if (it.resultCode == Activity.RESULT_OK) {
                    val returnUri: Uri? = it.data?.data
                    val intent = Intent(activity, FrPreview::class.java)

                    if (returnUri != null) {

                       val bitmap:Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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

    override fun onResume() {
        super.onResume()
        presenter.registerSensor()
    }

    override fun onStop() {
        super.onStop()
        presenter.unRegisterSensor()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onPause() {
        super.onPause()
        fr_camera_torch?.text = requireContext().resources.getString(R.string.touch_here_to_turn_light_on)
        fr_camera_torchh?.setImageDrawable(requireContext().getDrawable(R.drawable.ic_lightbulb))
        fr_linearLayout3.alpha = 0.5f
        presenter.unRegisterSensor()

    }



    override fun getViewActivity(): FragmentActivity? {
        return  activity
    }

    override fun startActivityFResult(intent: Intent, requestCode: Int) {
        startActivityForResult(intent, requestCode)
    }

    override fun showProgressDialog() {

           inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
           viewInflate =inflater.inflate(R.layout.rotated_progress_dialog,null)
           cl.alpha= 1F
           cl.addView(viewInflate)


    }


    override fun dismissProgressDialog() {
        cl.alpha= 0.5F
        cl.removeView(viewInflate)

    }
    override fun showToast() {
        Toast.makeText(
            activity, "This Image is not suitable (image is not landscape or resolution is low)",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun showAnim() {


           rotate.visibility=View.VISIBLE
           rotate.startAnimation(animation)


    }

    override fun dismissAnim() {

          rotate.visibility=View.GONE
          rotate.clearAnimation()
    }

    override fun showGallery() {
        val cameraIntent = Intent(Intent.ACTION_PICK)
        cameraIntent.type = "image/*"
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            myActivityResultLauncher.launch(cameraIntent)
        }
    }

    companion object{
    var viewInflate: View? = null
}
}