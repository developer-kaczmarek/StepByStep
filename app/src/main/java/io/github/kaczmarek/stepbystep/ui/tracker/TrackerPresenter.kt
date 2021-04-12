package io.github.kaczmarek.stepbystep.ui.tracker

import io.github.kaczmarek.stepbystep.ui.base.BasePresenter

class TrackerPresenter: BasePresenter<TrackerView>() {

    fun initIndicatorsValue() {
        viewState.initIndicatorsValue(0, 10000,0f, 0f, 0L)
    }
}