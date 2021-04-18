package io.github.kaczmarek.stepbystep.utils

import android.util.Log
import io.github.kaczmarek.stepbystep.BuildConfig

private const val defaultMessage = "Undefined error"

fun logError(tag: String, message: String?) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message ?: defaultMessage)
    }
}

fun logDebug(tag: String, message: String?) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, message ?: defaultMessage)
    }
}