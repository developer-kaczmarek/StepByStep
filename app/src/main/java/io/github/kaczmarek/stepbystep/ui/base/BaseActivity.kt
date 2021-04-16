package io.github.kaczmarek.stepbystep.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.utils.setTransparentStatusBar
import moxy.MvpAppCompatActivity

abstract class BaseActivity(@LayoutRes contentLayoutId: Int): MvpAppCompatActivity(contentLayoutId), BaseView {

    private var toolbar: Toolbar? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setTransparentStatusBar()
        toolbar = findViewById(R.id.toolbar)
        toolbar?.let {
            setSupportActionBar(it)
        }
    }

    fun showBackArrowButton() {
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun showAlertDialog(
        title: String?, message: String,
        posBtnTxt: String, negBtnTxt: String?,
        negBtnAction: () -> Unit, posBtnAction: () -> Unit,
        cancellable: Boolean
    ) {
        if (alertDialog?.isShowing != true) {
            alertDialog = MaterialAlertDialogBuilder(this,  R.style.DefaultAlertDialogTheme).apply {
                title?.let { setTitle(it) }
                setMessage(message)
                setPositiveButton(posBtnTxt) { _, _ -> posBtnAction() }
                setNegativeButton(negBtnTxt) { _, _ -> negBtnAction() }
                setCancelable(cancellable)
            }.show()
        }
    }
}