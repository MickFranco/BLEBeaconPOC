package com.mrzhevskyi.beaconpoc.fragment

import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.minew.beaconplus.sdk.MTCentralManager
import com.minew.beaconplus.sdk.MTPeripheral
import com.minew.beaconplus.sdk.enums.ConnectionStatus
import com.minew.beaconplus.sdk.exception.MTException
import com.minew.beaconplus.sdk.interfaces.ConnectionStatueListener
import com.minew.beaconplus.sdk.interfaces.GetPasswordListener
import com.mrzhevskyi.beaconpoc.Constants
import com.mrzhevskyi.beaconpoc.R
import com.mrzhevskyi.beaconpoc.ScannerService
import com.mrzhevskyi.beaconpoc.adapter.BeaconAdapter
import com.mrzhevskyi.beaconpoc.databinding.FragmentMainBinding
import com.mrzhevskyi.beaconpoc.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main){

    private lateinit var binding: FragmentMainBinding

    @Inject
    lateinit var manager: MTCentralManager

    private val adapter = BeaconAdapter{beacon ->
        connectBeacon(beacon)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkBluetooth()
        binding = FragmentMainBinding.bind(view)

        binding.rvBeacons.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBeacons.adapter = adapter

        binding.bScan.setOnClickListener {
            if(ScannerService.isRunning.value == true) stopService()
            else startService()
        }

        ScannerService.scannedBeacons.observe(viewLifecycleOwner,{ beacons ->
            adapter.setValues(beacons)
        })

        ScannerService.isRunning.observe(viewLifecycleOwner,{ running ->
            if(running) binding.bScan.text = getString(R.string.stop_scan)
            else binding.bScan.text = getString(R.string.start_scan)
        })

    }

    private fun checkBluetooth() {
        val bluetoothManager = requireActivity().getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        if(!adapter.isEnabled)adapter.enable()
    }


    private fun startService(){
        checkBluetooth()
        Intent(requireContext(),ScannerService::class.java).also {
            it.action = Constants.ACTION_START_SERVICE
            requireContext().startService(it)
        }
    }

    private fun stopService(){
        checkBluetooth()
        Intent(requireContext(),ScannerService::class.java).also {
            it.action = Constants.ACTION_STOP_SERVICE
            requireContext().startService(it)
        }
    }

    private fun connectBeacon(beacon: MTPeripheral){
        manager.connect(beacon,object: ConnectionStatueListener{
            override fun onUpdateConnectionStatus(status: ConnectionStatus?, p1: GetPasswordListener?) {
                lifecycleScope.launch {
                    val statusText = status?.name?:"ERROR"
                    statusText.apply {
                        showToast(this)
                        Timber.w(this)
                    }
                    when (status) {
                        ConnectionStatus.PASSWORDVALIDATING -> {
                            val builder = AlertDialog.Builder(requireContext())
                            val editText = EditText(requireContext())
                            editText.hint = getString(R.string.password)
                            builder.setView(editText)
                            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                                p1?.getPassword(editText.text.toString())
                            }
                            builder.show()
                        }
                        ConnectionStatus.COMPLETED -> {
                            DialogBeacon.create(beacon, childFragmentManager)
                        }
                        else -> {}
                    }
                }
            }

            override fun onError(e: MTException?) {
                Timber.e(e?.message)
                showToast(e?.message?:"ERROR")
            }

        })
    }

}