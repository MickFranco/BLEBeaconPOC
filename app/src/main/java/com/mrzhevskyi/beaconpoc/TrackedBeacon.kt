package com.mrzhevskyi.beaconpoc

import com.minew.beaconplus.sdk.MTPeripheral

data class TrackedBeacon(
    val mtPeripheral: MTPeripheral,
    var rssiLimit: Int = -80
)