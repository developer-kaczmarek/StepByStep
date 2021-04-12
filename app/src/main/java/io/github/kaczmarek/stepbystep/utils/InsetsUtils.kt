package io.github.kaczmarek.stepbystep.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AppInsets(windowInsets: WindowInsetsCompat) {
    val top = windowInsets.systemWindowInsetTop
    val bottom = windowInsets.systemWindowInsetBottom
}

interface InsetsListener {
    fun onApplyWindowInsets(view: View, insets: AppInsets)
}

fun View.applyWindowInsets(insetListener: InsetsListener) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        insetListener.onApplyWindowInsets(view, AppInsets(windowInsets))
        windowInsets
    }
    requestApplyInsetsWhenAttached()
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}