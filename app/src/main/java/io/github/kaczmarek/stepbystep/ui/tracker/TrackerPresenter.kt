package io.github.kaczmarek.stepbystep.ui.tracker

import android.location.Location
import android.location.LocationManager
import io.github.kaczmarek.domain.point.usecase.GetPointsUseCase
import io.github.kaczmarek.stepbystep.di.DIManager
import io.github.kaczmarek.stepbystep.ui.base.BasePresenter
import io.github.kaczmarek.stepbystep.utils.logDebug
import io.github.kaczmarek.stepbystep.utils.logError
import kotlinx.coroutines.launch
import moxy.presenterScope
import javax.inject.Inject

class TrackerPresenter : BasePresenter<TrackerView>() {

    @Inject
    lateinit var getPointsUseCase: GetPointsUseCase

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
                var realDistance = 0F
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
                val speeds: List<Float> = points.map { it.speed* 3.6F } // Перевод скорости в км/ч
                val averageSpeed = speeds.average()
                val maxSpeed = speeds.maxOrNull() ?: 0F
                val currentAccuracy = points.lastOrNull()?.accuracy ?: 0F
                val currentSpeed = points.lastOrNull()?.speed ?: 0F
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

    companion object {
        const val TAG = "TrackerPresenter"
    }
}