package io.github.kaczmarek.stepbystep.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*
import androidx.core.app.NotificationCompat
import android.os.Build
import io.github.kaczmarek.stepbystep.R
import java.util.*

class LocationService : Service(), LocationListener {

    var isRunning = false
    var locationManager: LocationManager? = null

    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        isRunning = false
        locationManager = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)
        return START_NOT_STICKY
    }

    override fun onLocationChanged(location: Location) {
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            manager?.createNotificationChannel(
                NotificationChannel(
                    getString(R.string.service_location_notification_channel_id),
                    getString(R.string.service_location_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    enableVibration(false)
                    enableLights(false)
                    setSound(null, null)
                }
            )
        }
        return NotificationCompat.Builder(
            this,
            getString(R.string.service_location_notification_channel_id)
        )
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.service_location_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.mipmap.ic_launcher
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

    }

    companion object {
        private const val TAG = "LocationService"
    }

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }
}