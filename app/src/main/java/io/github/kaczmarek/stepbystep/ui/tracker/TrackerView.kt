package io.github.kaczmarek.stepbystep.ui.tracker

import io.github.kaczmarek.stepbystep.ui.base.BaseView

interface TrackerView : BaseView {
    fun showInfoOfUnfinishedTrackIfPossible(
        realDistance: Float,
        currentSpeed: Float,
        maxSpeed: Float,
        averageSpeed: Double,
        duration: Long
    )

    fun updateInfoAboutCurrentTrack(
            realDistance: Float,
            currentAccuracy: Float,
            currentSpeed: Float,
            maxSpeed: Float,
            averageSpeed: Double
    )

    fun startTrackRecord(duration: Long)
    fun showGoalDistance(goalDistance: Int)
}