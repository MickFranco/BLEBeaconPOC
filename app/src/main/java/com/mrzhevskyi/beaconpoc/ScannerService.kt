package com.mrzhevskyi.beaconpoc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.minew.beaconplus.sdk.MTCentralManager
import com.minew.beaconplus.sdk.MTPeripheral
import com.mrzhevskyi.beaconpoc.Constants.ACTION_START_SERVICE
import com.mrzhevskyi.beaconpoc.Constants.ACTION_STOP_SERVICE
import com.mrzhevskyi.beaconpoc.Constants.NOTIFICATION_CHANNEL_ID
import com.mrzhevskyi.beaconpoc.Constants.NOTIFICATION_CHANNEL_NAME
import com.mrzhevskyi.beaconpoc.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ScannerService : LifecycleService() {

    @Inject
    lateinit var manager: MTCentralManager

    companion object {
        val scannedBeacons = MutableLiveData<List<MTPeripheral>>()
        val trackedBeacons = mutableListOf<TrackedBeacon>()
        val isRunning = MutableLiveData(false)
    }

    private var timer = CoroutineScope(Dispatchers.Main).launch { }

    private fun resetTimer() {
        timer.cancel()
        timer = CoroutineScope(Dispatchers.Main).launch {
            delay(Constants.NOTHING_FOUND_TIMER)
            scannedBeacons.postValue(emptyList())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when(intent?.action){
            ACTION_START_SERVICE -> startForeground()
            ACTION_STOP_SERVICE -> killSelf()
        }

        return START_STICKY
    }

    private fun startForeground() {
        isRunning.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle(getString(R.string.tracking_beacons))
            .setContentText("Tracking...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(
                PendingIntent.getActivity(
                    this.applicationContext, 0,
                    Intent(this.applicationContext, MainActivity::class.java),
                    PendingIntent.FLAG_MUTABLE
                )
            )

        startForeground(NOTIFICATION_ID, builder.build())

        manager.setMTCentralManagerListener { list ->
            Timber.w(list.size.toString())
            scannedBeacons.postValue(list)
            resetTimer()
        }
        manager.startService()
        manager.startScan()

        scannedBeacons.observe(this,{ list ->
            for(beacon in trackedBeacons){
                if(!list.contains(beacon.mtPeripheral)) launchAlarm(beacon)
                else if(list.single { it == beacon.mtPeripheral }.mMTFrameHandler.rssi < beacon.rssiLimit) launchAlarm(beacon)
            }
        })
    }

    private fun launchAlarm(beacon: TrackedBeacon){
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentTitle(getString(R.string.out_of_range))
            .setContentText(beacon.mtPeripheral.mMTFrameHandler.mac)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(
                PendingIntent.getActivity(
                    this.applicationContext, 0,
                    Intent(this.applicationContext, MainActivity::class.java),
                    PendingIntent.FLAG_MUTABLE
                )
            )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        notificationManager.notify(5,builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun killSelf(){
        isRunning.postValue(false)
        manager.stopScan()
        manager.stopService()
        stopForeground(true)
        stopSelf()
    }
}