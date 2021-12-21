package com.mrzhevskyi.beaconpoc.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.minew.beaconplus.sdk.MTCentralManager
import com.minew.beaconplus.sdk.MTPeripheral
import com.mrzhevskyi.beaconpoc.R
import com.mrzhevskyi.beaconpoc.databinding.DialogBeaconBinding
import com.mrzhevskyi.beaconpoc.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DialogBeacon: DialogFragment(R.layout.dialog_beacon) {

    private var beacon: MTPeripheral? = null

    @Inject
    lateinit var manager: MTCentralManager

    companion object{
        fun create(beacon: MTPeripheral, manager: FragmentManager){
            val dialog = DialogBeacon()
            dialog.beacon = beacon
            dialog.show(manager,"dialog")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = DialogBeaconBinding.bind(view)

        binding.tvMac.text = beacon?.mMTFrameHandler?.mac
        binding.bDisconnect.setOnClickListener {
            dismiss()
        }
        binding.bTurnOff.setOnClickListener {
            beacon?.mMTConnectionHandler?.powerOff { _, _ -> }
            showToast("TURNED OFF")
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        manager.disconnect(beacon)
        super.onDismiss(dialog)
    }
}