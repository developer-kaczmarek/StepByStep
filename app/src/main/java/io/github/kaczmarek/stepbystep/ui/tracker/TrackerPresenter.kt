package io.github.kaczmarek.stepbystep.ui.tracker

import android.location.Location
import android.location.LocationManager
import io.github.kaczmarek.domain.point.usecase.DeleteAllPointsUseCase
import io.github.kaczmarek.domain.point.usecase.GetPointsUseCase
import io.github.kaczmarek.domain.track.entity.TrackEntity
import io.github.kaczmarek.domain.track.usecase.GetLastUnfinishedTrackUseCase
import io.github.kaczmarek.domain.track.usecase.GetTrackCountUseCase
import io.github.kaczmarek.domain.track.usecase.SaveTrackUseCase
import io.github.kaczmarek.stepbystep.di.DIManager
import io.github.kaczmarek.stepbystep.ui.base.BasePresenter
import io.github.kaczmarek.stepbystep.utils.getNumberOrZero
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

    @Inject
    lateinit var getLastUnfinishedTrackUseCase: GetLastUnfinishedTrackUseCase

    @Inject
    lateinit var deleteAllPointsUseCase: DeleteAllPointsUseCase

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

    fun checkLastUnfinishedTrack() {
        presenterScope.launch {
            try {
                realDistance = 0F
                val track = getLastUnfinishedTrackUseCase.execute()
                viewState.showInfoOfUnfinishedTrackIfPossible(
                    track?.distance ?: realDistance,
                    track?.currentSpeed ?: currentSpeed,
                    track?.maxSpeed ?: maxSpeed,
                    track?.averageSpeed ?: averageSpeed,
                    track?.duration ?: 0L
                )
            } catch (e: Exception) {
                logError(TAG, e.message)
            }
        }
    }

    fun getActualTrackInformation() {
        presenterScope.launch {
            try {
                realDistance = 0F
                val points = getPointsUseCase.execute()
                var lastLocation = Location(LocationManager.GPS_PROVIDER)

                points.forEachIndexed { index, item ->
                    if (index == 0) {
                        with(lastLocation) {
                            latitude = item.latitude
                            longitude = item.latitude
                            altitude = item.altitude
                            accuracy = item.accuracy
                            speed = item.speed
                        }
                    } else {
                        val location = Location(LocationManager.GPS_PROVIDER)
                        with(location) {
                            latitude = item.latitude
                            longitude = item.latitude
                            altitude = item.altitude
                            accuracy = item.accuracy
                            speed = item.speed
                        }
                        val distanceBetweenPoints = lastLocation.distanceTo(location)
                        if (distanceBetweenPoints > location.accuracy) {
                            realDistance += distanceBetweenPoints
                        }
                        lastLocation = location
                    }
                }
                val currentAccuracy = getNumberOrZero(points.lastOrNull()?.accuracy ?: 0F)
                val speeds: List<Float> = points.map { it.speed * 3.6F } // Перевод скорости в км/ч
                averageSpeed = getNumberOrZero(speeds.average())
                maxSpeed = getNumberOrZero(speeds.maxOrNull() ?: 0F)
                currentSpeed = getNumberOrZero(points.lastOrNull()?.speed ?: 0F)

                viewState.updateInfoAboutCurrentTrack(
                    realDistance,
                    currentAccuracy,
                    currentSpeed,
                    maxSpeed,
                    averageSpeed
                )
            } catch (e: Exception) {
                logError(TAG, e.message)
            }
        }
    }

    fun saveCurrentTrack(duration: Long, isFinishedRecord: Boolean) {
        presenterScope.launch {
            try {
                val lastUnfinishedTrack = getLastUnfinishedTrackUseCase.execute()
                lastUnfinishedTrack?.let {
                    saveTrackUseCase.execute(
                        it.copy(
                            distance = realDistance,
                            duration = duration,
                            maxSpeed = maxSpeed,
                            averageSpeed = averageSpeed,
                            currentSpeed = currentSpeed,
                            isFinishedRecord = isFinishedRecord
                        )
                    )
                }
            } catch (e: Exception) {
                logError(TAG, e.message)
            }
        }
    }

    fun startOrResumeTrack() {
        presenterScope.launch {
            try {
                val lastUnfinishedTrack = getLastUnfinishedTrackUseCase.execute()
                if (lastUnfinishedTrack == null) {
                    deleteAllPointsUseCase.execute()
                    val trackCount = getTrackCountUseCase.execute()
                    val currentFormattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                    val id = currentFormattedDate + trackCount
                    saveTrackUseCase.execute(
                        TrackEntity(
                            id,
                            null,
                            0L,
                            null,
                            null,
                            null,
                            isFinishedRecord = false
                        )
                    )
                }
                viewState.startTrackRecord(lastUnfinishedTrack?.duration ?: 0L)
            } catch (e: Exception) {
                logError(TAG, e.message)
            }
        }
    }

    fun getGoalDistance() {
        // Здесь будет обращение до SharedPrefs через UseCase
        presenterScope.launch {
            try {
                viewState.showGoalDistance(GOAL_DISTANCE)
            } catch (e: Exception) {
                logError(TAG, e.message)
            }
        }
    }

    companion object {
        const val TAG = "TrackerPresenter"

        const val GOAL_DISTANCE = 4500 // Перенести в настройки
    }
}