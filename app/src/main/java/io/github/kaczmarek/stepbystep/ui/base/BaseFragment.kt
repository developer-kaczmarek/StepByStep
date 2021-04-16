package io.github.kaczmarek.stepbystep.ui.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.utils.InsetsListener
import io.github.kaczmarek.stepbystep.utils.applyWindowInsets
import moxy.MvpAppCompatFragment

abstract class BaseFragment(@LayoutRes contentLayoutId: Int) :
    MvpAppCompatFragment(contentLayoutId), InsetsListener {

    protected var toolbar: Toolbar? = null

    private var baseActivity: BaseActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as? BaseActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.applyWindowInsets(this)
        toolbar = view.findViewById(R.id.toolbar)
        toolbar?.let {
            baseActivity?.setSupportActionBar(toolbar)
        }
    }

    open fun showAlertDialog(
        title: String? = null,
        message: String,
        posBtnTxt: String = getString(R.string.common_ok),
        negBtnTxt: String? = null,
        negBtnAction: () -> Unit = {},
        posBtnAction: () -> Unit = {},
        cancellable: Boolean = true
    ) {
        baseActivity?.showAlertDialog(
            title,
            message,
            posBtnTxt,
            negBtnTxt,
            negBtnAction,
            posBtnAction,
            cancellable
        )
    }

    fun showBackArrowButton() {
        baseActivity?.showBackArrowButton()
    }
}