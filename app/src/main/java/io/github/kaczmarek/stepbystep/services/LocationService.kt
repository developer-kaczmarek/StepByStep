package io.github.kaczmarek.stepbystep.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*
import androidx.core.app.NotificationCompat
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import io.github.kaczmarek.domain.point.entity.PointEntity
import io.github.kaczmarek.domain.point.usecase.SavePointUseCase
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.di.DIManager
import io.github.kaczmarek.stepbystep.utils.logDebug
import io.github.kaczmarek.stepbystep.utils.logError
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.util.*
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class LocationService : Service(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + supervisorJob

    @Inject
    lateinit var savePointUseCase: SavePointUseCase

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private val binder = LocalBinder()
    private val supervisorJob: Job by lazy { SupervisorJob() }
    private var updateCurrentLocationJob: Job? = null

    init {
        DIManager.getLocationServiceSubcomponent().inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        supervisorJob.cancel()
        updateCurrentLocationJob?.cancel()
        locationManager = null
        DIManager.removeLocationServiceSubcomponent()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        updateCurrentLocationJob = launch {
            getLocationDataFlow().collect {
                try {
                    val result = it.getOrNull()
                    result?.let { location ->
                        logDebug(TAG, "location = $location")
                        savePointUseCase.execute(
                            PointEntity(
                                location.latitude,
                                location.longitude,
                                location.altitude,
                                location.accuracy,
                                location.speed
                            )
                        )
                    }
                } catch (e: Exception) {
                    logError(TAG, e.message)
                }
            }
        }

        return START_NOT_STICKY
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
            .setContentTitle(getString(R.string.service_location_notification_title))
            .setContentText(getString(R.string.service_location_notification_text))
            .setSmallIcon(R.drawable.ic_record_location_24)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_record_location_24))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
    }

    @Throws(
        LocationException::class,
        GPSDisabledException::class,
        SecurityException::class,
        TimeoutException::class
    )
    suspend fun getCurrentLocation(): Location {
        val coroutine = CompletableDeferred<Location>()
        when {
            locationManager == null -> coroutine.completeExceptionally(LocationException("Location manager not found"))
            !isLocationPermissionGranted(this) -> coroutine.completeExceptionally(
                SecurityException("Required permission denied")
            )
            !isGPSEnabled() -> coroutine.completeExceptionally(GPSDisabledException())
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    locationManager?.getCurrentLocation(
                        LocationManager.GPS_PROVIDER,
                        null,
                        mainExecutor
                    ) { location ->
                        coroutine.complete(location)
                    }
                } else {
                    locationListener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            coroutine.complete(location)
                        }

                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {
                            coroutine.completeExceptionally(LocationException("Status $provider changed: $status with extras $extras"))
                        }

                        override fun onProviderDisabled(provider: String) {
                            coroutine.completeExceptionally(LocationException("Provider $provider disabled"))
                        }
                    }
                    locationListener?.let {
                        @Suppress("DEPRECATION")
                        locationManager?.requestSingleUpdate(
                            LocationManager.GPS_PROVIDER,
                            it,
                            mainLooper
                        )
                    }
                }

                try {
                    withTimeout(TIMEOUT) {
                        coroutine.await()
                    }
                } catch (e: TimeoutCancellationException) {
                    if (coroutine.isActive) {
                        coroutine.completeExceptionally(TimeoutException("Location timeout on $TIMEOUT ms"))
                    }
                } finally {
                    locationListener?.let { locationManager?.removeUpdates(it) }
                }
            }
        }
        return coroutine.await()
    }

    private fun getLocationDataFlow() = flow {
        while (true) {
            try {
                val location = getCurrentLocation()
                emit(Result.success(location))
            } catch (e: Exception) {
                emit(Result.failure<Location>(e))
            }
            delay(INTERVAL)
        }
    }

    private fun isGPSEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    companion object {
        private const val TAG = "LocationService"

        //Будущие настройки
        const val TIMEOUT = 30000L
        const val INTERVAL = 15000L
    }

    class LocationException(message: String) : Exception(message)

    class GPSDisabledException : Exception("Location adapter turned off on device")

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }
}

fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}