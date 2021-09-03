package com.hms.referenceapp.huaweilens.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.qr.`interface`.QrInterface
import com.hms.referenceapp.huaweilens.qr.presenter.QrPresenter
import com.huawei.hms.mlsdk.common.MLApplication
import kotlinx.android.synthetic.main.fragment_qr.*

class QrFragment() : Fragment(), QrInterface.CameraView {
    private lateinit var presenter: QrInterface.CameraPresenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewCamera = inflater.inflate(R.layout.fragment_qr, container, false)
        MLApplication.getInstance().apiKey =
            "CgB6e3x9IdI9DYYI/IY9T4SBMt5hX0pzZjznmMu4hJ7xmGE/N15RR7VHfFwBi4zA6Vyx58vIhS1XGty1ZU2JpR6H"
        return viewCamera
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = QrPresenter(this,savedInstanceState)
       presenter.init()
        presenter.checkPerms()
   //    presenter.initCamera()
        presenter.initButtonCallbacks()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onFragmentDestroy()
    }

    override fun onStop() {
        super.onStop()
        presenter.onFragmentStop()
    }

    override fun onStart() {
        super.onStart()
        presenter.onFragmentStart()
    }

    override fun onPause() {
        super.onPause()
        presenter.onFragmentPause();
        tv_camera_torch?.text = requireContext().resources.getString(R.string.touch_here_to_turn_light_on)
        iv_camera_torch?.setImageDrawable(requireContext().getDrawable(R.drawable.ic_lightbulb))
        linearLayout3.alpha = 0.5f
    }

    override fun onResume() {
        super.onResume()
        presenter.onFragmentResume()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 3){
            presenter.init()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val title = "Qr Fragment"
    }
}