package io.github.kaczmarek.stepbystep.ui.tracker

import io.github.kaczmarek.stepbystep.ui.base.BaseView

interface TrackerView : BaseView {
    fun initIndicatorsValue(
            realDistance: Float,
            goalDistance: Int,
            currentAccuracy: Float,
            currentSpeed: Float,
            maxSpeed: Float,
            averageSpeed: Double,
            time: Long,
            satellitesCount: Int
    )

    fun updateParamsValue(
            realDistance: Float,
            currentAccuracy: Float,
            currentSpeed: Float,
            maxSpeed: Float,
            averageSpeed: Double
    )
}