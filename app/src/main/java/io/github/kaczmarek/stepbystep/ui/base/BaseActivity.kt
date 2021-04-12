package io.github.kaczmarek.stepbystep.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import io.github.kaczmarek.stepbystep.R
import io.github.kaczmarek.stepbystep.utils.setTransparentStatusBar
import moxy.MvpAppCompatActivity

abstract class BaseActivity(@LayoutRes contentLayoutId: Int): MvpAppCompatActivity(contentLayoutId), BaseView {

    private var toolbar: Toolbar? = null

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
}