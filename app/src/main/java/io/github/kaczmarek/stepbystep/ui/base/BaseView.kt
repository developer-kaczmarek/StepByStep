package io.github.kaczmarek.stepbystep.ui.base

import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.utils.Utils.getString
import moxy.MvpView

interface BaseView : MvpView {
    fun showAlertDialog(
        title: String? = null,
        message: String,
        posBtnTxt: String = getString(R.string.common_ok),
        negBtnTxt: String? = null,
        negBtnAction: () -> Unit = {},
        posBtnAction: () -> Unit = {},
        cancellable: Boolean = true
    )
}