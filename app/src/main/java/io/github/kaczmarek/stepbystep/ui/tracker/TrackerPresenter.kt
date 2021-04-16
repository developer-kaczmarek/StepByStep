package io.github.kaczmarek.stepbystep.ui.tracker

import android.location.Location
import android.location.LocationManager
import io.github.kaczmarek.domain.point.usecase.GetPointsUseCase
import io.github.kaczmarek.domain.track.entity.TrackEntity
import io.github.kaczmarek.domain.track.usecase.GetTrackCountUseCase
import io.github.kaczmarek.domain.track.usecase.SaveTrackUseCase
import io.github.kaczmarek.stepbystep.di.DIManager
import io.github.kaczmarek.stepbystep.ui.base.BasePresenter
import io.github.kaczmarek.stepbystep.utils.logDebug
import io.github.kaczmarek.stepbystep.utils.logError
import kotlinx.coroutines.launch
import moxy.presenterScope
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TrackerPresenter : BasePresenter<TrackerView>() {

    @Inject
    lateinit var getPointsUseCase: GetPointsUseCase

    @Inject
    lateinit var saveTrackUseCase: SaveTrackUseCase

    @Inject
    lateinit var getTrackCountUseCase: GetTrackCountUseCase

    private var realDistance = 0F
    private var averageSpeed = 0.0
    private var maxSpeed = 0F
    private var currentSpeed = 0F

    init {
        DIManager.getTrackerSubcomponent().inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DIManager.removeTrackerSubcomponent()
    }

    fun initIndicatorsValue() {
        // Временное решение
        viewState.initIndicatorsValue(
            0F,
            4500,
            0F,
            0F,
            0F,
            0.0,
            0L,
            0
        )
    }

    fun getActualTrackInformation() {
        presenterScope.launch {
            try {
                realDistance = 0F
                val points = getPointsUseCase.execute()
                var lastLocation = Location(LocationManager.GPS_PROVIDER)

                points.forEachIndexed { index, pointEntity ->
                    if (index == 0) {
                        with(lastLocation) {
                            latitude = pointEntity.latitude
                            longitude = pointEntity.latitude
                            altitude = pointEntity.altitude
                            accuracy = pointEntity.accuracy
                            speed = pointEntity.speed
                        }
                    } else {
                        val location = Location(LocationManager.GPS_PROVIDER)
                        with(location) {
                            latitude = pointEntity.latitude
                            longitude = pointEntity.latitude
                            altitude = pointEntity.altitude
                            accuracy = pointEntity.accuracy
                            speed = pointEntity.speed
                        }
                        val distanceBetweenPoints = lastLocation.distanceTo(location)
                        if (distanceBetweenPoints > location.accuracy) {
                            realDistance += distanceBetweenPoints
                        }
                        lastLocation = location
                    }
                }
                val currentAccuracy = points.lastOrNull()?.accuracy ?: 0F
                val speeds: List<Float> = points.map { it.speed * 3.6F } // Перевод скорости в км/ч
                averageSpeed = speeds.average()
                maxSpeed = speeds.maxOrNull() ?: 0F
                currentSpeed = points.lastOrNull()?.speed ?: 0F

                viewState.updateParamsValue(
                    realDistance,
                    currentAccuracy,
                    currentSpeed,
                    maxSpeed,
                    averageSpeed
                )
                logDebug(
                    TAG,
                    "distance = $realDistance, currentAccuracy = $currentAccuracy, averageSpeed = $averageSpeed, maxSpeed = $maxSpeed"
                )
            } catch (e: Exception) {
                logError(TAG, e.message)
            }
        }
    }

    fun saveCurrentTrack(recordTime: Long, isFinishedRecord: Boolean) {
        presenterScope.launch {
            try {
                val trackCount = getTrackCountUseCase.execute()
                val dateForId = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                saveTrackUseCase.execute(
                    TrackEntity(
                        dateForId + trackCount,
                        realDistance,
                        recordTime,
                        maxSpeed,
                        averageSpeed,
                        currentSpeed,
                        isFinishedRecord
                    )
                )
            } catch (e: Exception) {
                logError(TAG, e.message)
            }
        }
    }

    companion object {
        const val TAG = "TrackerPresenter"
    }
}