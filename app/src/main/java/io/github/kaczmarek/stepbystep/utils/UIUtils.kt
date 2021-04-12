package io.github.kaczmarek.stepbystep.utils

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController

@Suppress("DEPRECATION")
fun Window.setTransparentStatusBar() {
    apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setDecorFitsSystemWindows(false)
            insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

/**
 * Помогает сделать видимым View
 */
inline val View.visible: View
    get() = apply { visibility = View.VISIBLE }

/**
 * Помогает сделать невидимым View и не оставляет место для него
 */
inline val View.gone: View
    get() = apply { visibility = View.GONE }