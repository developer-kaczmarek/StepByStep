package io.github.kaczmarek.stepbystep.ui.tracker

import io.github.kaczmarek.stepbystep.ui.base.BaseView

interface TrackerView : BaseView {
    fun initIndicatorsValue(steps: Int, goalSteps: Int, distance: Float, energy: Float, time: Long)
    fun resetIndicators(steps: Int, distance: Float, energy: Float, time: Long)
}