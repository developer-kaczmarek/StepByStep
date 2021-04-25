package io.github.kaczmarek.stepbystep.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import androidx.core.app.NotificationCompat
import io.github.kaczmarek.domain.point.entity.PointEntity
import io.github.kaczmarek.domain.point.usecase.SavePointUseCase
import io.github.kaczmarek.domain.track.usecase.GetLastUnfinishedTrackUseCase
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.di.DIManager
import io.github.kaczmarek.stepbystep.ui.main.MainActivity
import io.github.kaczmarek.stepbystep.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TrackerService : Service(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + supervisorJob

    @Inject
    lateinit var savePointUseCase: SavePointUseCase

    @Inject
    lateinit var getLastUnfinishedTrackUseCase: GetLastUnfinishedTrackUseCase

    private var timeRecordingInSecond: Long = 0L
    private var realDistance: Float = 0F
    private var lastLocation: Location? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private val supervisorJob: Job by lazy { SupervisorJob() }
    private var updateCurrentLocationJob: Job? = null
    private var recordTimerJob: Job? = null
    private val timer = Timer()
    private var taskCalculatingDurationAndDistance: TimerTask? = null

    init {
        DIManager.getTrackerServiceSubcomponent().inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        locationListener?.let { locationManager?.removeUpdates(it) }
        updateCurrentLocationJob?.cancel()
        taskCalculatingDurationAndDistance?.cancel()
        timer.cancel()
        recordTimerJob?.cancel()
        supervisorJob.cancel()
        DIManager.removeTrackerServiceSubcomponent()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initForegroundNotification()
        startRecordTrack()
        return START_NOT_STICKY
    }

    /**
     * Метод позволяет получить актуальную локацию пользователя из GPS-сервиса устройства с [TIMEOUT]
     * таймаутом на получение координат (который в дальнейшем будет браться из SharedPreferences).
     * @throws LocationException Ошибка вызывается при различных ситуациях, проблема которых описана в message.
     * @throws GPSDisabledException Ошибка вызывается при выключенной настройки определения местоположения.
     * @throws SecurityException Ошибка вызывается при отсутствии разрешения на получение геолокации.
     * @throws TimeoutException Ошибка вызывается в случае, если время ожидания запроса геолокациии привысило значение [TIMEOUT].
     * @return [Location] Локация пользователя.
     */
    @Throws(
        LocationException::class,
        GPSDisabledException::class,
        SecurityException::class,
        TimeoutException::class
    )
    suspend fun getCurrentLocation(): Location {
        val coroutine = CompletableDeferred<Location>()
        when {
            locationManager == null -> coroutine.completeExceptionally(
                LocationException("Location manager not found")
            )
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

    /**
     * Метод позволяет создать поток данных, в котором после интервала [INTERVAL]
     * (который в дальнейшем будет браться из SharedPreferences),
     * делается запрос на обновление геолокации устройства.
     */
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

    /**
     * Метод для инициализации и отображения уведомления,
     * отображающий работу сервиса записи трека.
     * Для устройств на Android 8 и выше создается канал уведомлений при необходимости.
     */
    private fun initForegroundNotification() {
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
        val notification = getNotification(getString(R.string.service_location_notification_text))
        startForeground(NOTIFICATION_ID, notification)
    }

    /**
     * Метод для формирования уведомления сервиса записи трека,
     * при клике на который произойдет открытие приложения.
     * @param contextText Контент для отображения в уведомлении.
     * @return [Notification] Уведомление для отображения.
     */
    private fun getNotification(contextText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(
            this,
            getString(R.string.service_location_notification_channel_id)
        )
            .setContentTitle(getString(R.string.service_location_notification_title))
            .setContentText(contextText)
            .setSmallIcon(R.drawable.ic_record_location_24)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_record_location_24))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * Метод для расчета пройденной дистанции на основе координат
     * между старой координатной точкой и текущей.
     * @param currentLocationPoint Текущее значение Location.
     */
    private fun calculateDistance(currentLocationPoint: Location) {
        val distanceBetweenPoints = lastLocation?.distanceTo(currentLocationPoint) ?: 0F
        if (distanceBetweenPoints > currentLocationPoint.accuracy) {
            realDistance += distanceBetweenPoints
        }
        lastLocation = currentLocationPoint
    }

    /**
     * Метод для проверки активированности GPS-адаптера на устройстве.
     * @return [Boolean] Флаг, возвращает включен ли GPS-адаптера на устройстве.
     */
    private fun isGPSEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    private fun startRecordTrack() {
        updateCurrentLocationJob = launch {
            getLocationDataFlow().collect {
                try {
                    val result = it.getOrNull()
                    result?.let { location ->
                        savePointUseCase.execute(
                            PointEntity(
                                getNumberOrZero(location.latitude),
                                getNumberOrZero(location.longitude),
                                getNumberOrZero(location.altitude),
                                getNumberOrZero(location.accuracy),
                                getNumberOrZero(location.speed)
                            )
                        )
                        calculateDistance(location)
                    }
                } catch (e: Exception) {
                    logError(TAG, e.message)
                }
            }
        }

        recordTimerJob = launch {
            try {
                timeRecordingInSecond = getLastUnfinishedTrackUseCase.execute()?.duration ?: 0L
                taskCalculatingDurationAndDistance = object : TimerTask() {
                    override fun run() {
                        val notification = getNotification(
                            getString(
                                R.string.service_location_notification_record_text,
                                TimeUnit.MILLISECONDS.toHours(timeRecordingInSecond),
                                TimeUnit.MILLISECONDS.toMinutes(timeRecordingInSecond) % TimeUnit.HOURS.toMinutes(1),
                                TimeUnit.MILLISECONDS.toSeconds(timeRecordingInSecond) % TimeUnit.MINUTES.toSeconds(1),
                                this@TrackerService.getFormattedDistance(realDistance)
                            )
                        )
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                        manager?.notify(NOTIFICATION_ID, notification)
                        timeRecordingInSecond += 1000
                    }
                }
                timer.scheduleAtFixedRate(taskCalculatingDurationAndDistance, 0, 1000)
            } catch (e: Exception) {
                logError(TAG, e.message)
            }
        }
    }

    companion object {
        //Будущие настройки
        const val TIMEOUT = 30000L
        const val INTERVAL = 15000L

        private const val NOTIFICATION_ID = 564
        private const val TAG = "LocationService"
    }

    /**
     * Ошибка получения геолокации, с возможностью переопределения сообщения.
     * @param message Описание причины возникновения ошибки.
     */
    class LocationException(message: String) : Exception(message)

    /**
     * Ошибка, которая вызывается при выключенном состоянии
     * настройки определения местоположения на устройстве.
     */
    class GPSDisabledException : Exception("Location adapter turned off on device")
}