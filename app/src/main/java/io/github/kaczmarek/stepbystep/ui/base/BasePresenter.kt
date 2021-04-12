package io.github.kaczmarek.stepbystep.ui.base

import moxy.MvpPresenter
import moxy.MvpView

abstract class BasePresenter<View : MvpView> : MvpPresenter<View>()