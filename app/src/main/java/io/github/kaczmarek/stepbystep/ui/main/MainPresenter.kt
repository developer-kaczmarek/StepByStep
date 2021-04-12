package io.github.kaczmarek.stepbystep.ui.main

import io.github.kaczmarek.stepbystep.ui.base.BasePresenter

class MainPresenter: BasePresenter<MainView>() {
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.launchStepsScreen()
    }
}