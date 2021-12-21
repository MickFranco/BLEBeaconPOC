package com.mrzhevskyi.beaconpoc

import com.minew.beaconplus.sdk.MTPeripheral

fun MTPeripheral.isTracked(): Boolean {
    for(beacon in ScannerService.trackedBeacons){
        if(beacon.mtPeripheral.mMTFrameHandler.mac == this.mMTFrameHandler.mac) return true
    }
    return false
}

fun MTPeripheral.track(limit: Int){
    var contains = false
    for(beacon in ScannerService.trackedBeacons){
        if(beacon.mtPeripheral.mMTFrameHandler.mac == this.mMTFrameHandler.mac) contains = true
    }
    if(!contains) ScannerService.trackedBeacons.add(TrackedBeacon(this,limit))
}

fun MTPeripheral.stopTracking(){
    for(beacon in ScannerService.trackedBeacons.toMutableList()){
        if(beacon.mtPeripheral.mMTFrameHandler.mac == this.mMTFrameHandler.mac) ScannerService.trackedBeacons.remove(beacon)
    }
}

fun MTPeripheral.getRssiLimit(): Int{
    for(beacon in ScannerService.trackedBeacons){
        if(beacon.mtPeripheral.mMTFrameHandler.mac == this.mMTFrameHandler.mac) return beacon.rssiLimit
    }
    return -80;
}

fun MTPeripheral.setRssiLimit(limit: Int){
    for(beacon in ScannerService.trackedBeacons){
        if(beacon.mtPeripheral.mMTFrameHandler.mac == this.mMTFrameHandler.mac) beacon.rssiLimit = limit
    }
}