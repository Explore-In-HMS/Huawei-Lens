package com.hms.referenceapp.huaweilens.qr.presenter

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.bcr.activity.ImagePreviewActivity
import com.hms.referenceapp.huaweilens.bcr.util.BitmapUtils
import com.hms.referenceapp.huaweilens.main.MenuActivity
import com.hms.referenceapp.huaweilens.main.widgets.CameraXHelper
import com.hms.referenceapp.huaweilens.qr.QrFragment
import com.hms.referenceapp.huaweilens.qr.`interface`.QrInterface
import com.hms.referenceapp.huaweilens.qr.view.ScanningView
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.ml.scan.HmsScan
import kotlinx.android.synthetic.main.fragment_business_card.*
import kotlinx.android.synthetic.main.fragment_qr.linearLayout3
import kotlinx.android.synthetic.main.fragment_qr.tv_camera_torch
import kotlinx.android.synthetic.main.fragment_qr.iv_camera_torch
import kotlinx.android.synthetic.main.fragment_qr.*
import kotlinx.android.synthetic.main.fragment_qr.view.*


class QrPresenter(var view: QrFragment, var savedInstanceState: Bundle?) :
     QrInterface.CameraPresenter,
    LifecycleOwner {

    private lateinit var scanningView: ScanningView
    private var remoteView: RemoteView? = null
    var mScreenWidth = 0
    var mScreenHeight = 0
    //scan_view_finder width & height is  300dp
    val SCAN_FRAME_SIZE = 300

    override fun checkPerms() {
        if (ContextCompat.checkSelfPermission(
                view.requireActivity(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                view.requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) else {
            view.requireActivity().requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 3
            )
        }
    }

    override fun init()
    {
        scanningView = view.requireView().findViewById(R.id.scanningView)
        scanningView.startAnimation() //To start Animation

        val dm = view.requireActivity().resources.displayMetrics
        //2.get screen size
        val density = dm.density
        mScreenWidth=dm.widthPixels
        mScreenHeight=dm.heightPixels
        var scanFrameSize=(SCAN_FRAME_SIZE*density)
        //3.caculate viewfinder's rect,it's in the middle of the layout
        //set scanning area(Optional, rect can be null,If not configure,default is in the center of layout)
        val rect = Rect()
        apply {
            rect.left = (mScreenWidth / 2 - scanFrameSize / 2).toInt()
            rect.right = (mScreenWidth / 2 + scanFrameSize / 2).toInt()
            rect.top = (mScreenHeight / 2 - scanFrameSize / 2).toInt()
            rect.bottom = (mScreenHeight / 2 + scanFrameSize / 2).toInt()
        }


        //view.linearLayout4.set
        remoteView = RemoteView.Builder().setContext(view.requireActivity()).setBoundingBox(rect).setFormat(
            HmsScan.ALL_SCAN_TYPE).build()

        remoteView?.onCreate(savedInstanceState)
        remoteView?.setOnResultCallback { result ->
            if (result != null && result.size > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {

                if(result[0].scanTypeForm == HmsScan.CONTACT_DETAIL_FORM)
                {
                    val intent = Intent(Intent.ACTION_INSERT)
                    intent.type = ContactsContract.Contacts.CONTENT_TYPE
                    if(result[0].getContactDetail().getPeopleName() != null)
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, result[0].getContactDetail().getPeopleName().fullName)
                    if(result[0].getContactDetail().getCompany() != null)
                    intent.putExtra(ContactsContract.Intents.Insert.COMPANY, result[0].getContactDetail().getCompany())
                    if(result[0].getContactDetail().getTelPhoneNumbers().size != 0)
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, result[0].getContactDetail().getTelPhoneNumbers()[0].telPhoneNumber)
                    if(result[0].getContactDetail().getTelPhoneNumbers().size > 1)
                    intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, result[0].getContactDetail().getTelPhoneNumbers()[1].telPhoneNumber)
                    if(result[0].getContactDetail().eMailContents.isNotEmpty())
                    intent.putExtra(ContactsContract.Intents.Insert.EMAIL, result[0].getContactDetail().eMailContents[0].addressInfo)
                    if(result[0].getContactDetail().getAddressesInfos().size != 0)
                    intent.putExtra(ContactsContract.Intents.Insert.POSTAL, result[0].getContactDetail().getAddressesInfos()[0].addressDetails[0])
                    view.requireActivity().startActivity(intent)
                }
                if(result[0].scanTypeForm == HmsScan.SMS_FORM)
                {
                    val intent = Intent(Intent.ACTION_VIEW,Uri.fromParts("sms",  result[0].getSmsContent().destPhoneNumber, null))
                    intent.putExtra("sms_body",result[0].getSmsContent().msgContent)
                    view.requireActivity().startActivity(intent)
                }
                if(result[0].scanTypeForm == HmsScan.TEL_PHONE_NUMBER_FORM)
                {
                    val intent = Intent(Intent.ACTION_DIAL ,Uri.parse("tel:"+result[0].getTelPhoneNumber().telPhoneNumber))
                    view.requireActivity().startActivity(intent)
                }
                if(result[0].scanTypeForm == HmsScan.URL_FORM)
                {
                    val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(result[0].originalValue))
                    view.requireActivity().startActivity(intent)
                }
            }
        }
        val params = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        val frameLayout = view.requireView().findViewById<FrameLayout>(R.id.rim1)
        frameLayout.addView(remoteView, params)


    }

    override fun initButtonCallbacks() {
        view.linearLayout3.setOnClickListener {
            if(remoteView?.lightStatus == false)
            {
                it.alpha = 1f
                view.iv_camera_torch.setImageDrawable(
                    ContextCompat.getDrawable(view.requireContext(), R.drawable.ic_lightbulb_on)
                )
                view.tv_camera_torch.text = view.resources.getString(R.string.touch_here_to_turn_light_off)
            }
            else
            {
                it.alpha = .5f
                view.iv_camera_torch.setImageDrawable(
                    ContextCompat.getDrawable(view.requireContext(), R.drawable.ic_lightbulb)
                )
                view.tv_camera_torch.text = view.resources.getString(R.string.touch_here_to_turn_light_on)
            }
            remoteView?.switchLight()
         }
    }

    override fun onFragmentStart() {
       // remoteView?.onStart()
      //  remoteView?.onCreate(savedInstanceState)
        remoteView?.onStop()
    }

    override fun onFragmentStop() {
      //  init()
      //  remoteView?.onStop()
    }

    override fun onFragmentPause() {
        remoteView?.onPause()
    }

    override fun onFragmentResume() {
        init()
        remoteView?.onResume()
    }

    override fun onFragmentDestroy() {
        remoteView?.onDestroy()
    }


    override fun getLifecycle(): Lifecycle {
        return view.lifecycle
    }

    companion object {
        private const val REQUEST_IMAGE_SELECT_FROM_ALBUM = 1000
        private const val REQUEST_IMAGE_CAPTURE = 1001
    }

}