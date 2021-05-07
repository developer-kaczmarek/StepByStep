package io.github.kaczmarek.stepbystep.utils

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import io.github.kaczmarek.stepbystep.R
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

object Utils {
    private var application: Application? = null

    fun init(application: Application) {
        this.application = application
    }

    fun getString(@StringRes id: Int, vararg parameters: Any): String {
        return application?.getString(id, *parameters)
            ?: throw IllegalStateException(
                "Application context in Utils not initialized.Please " +
                    "call method init in your Application instance"
            )
    }

    fun Float.addLengthUnit(): String {
        return if (this < 1000F) {
            getString(R.string.common_m, DecimalFormat("#.##").format(if (this.isNaN()) 0F else this))
        } else {
            getString(R.string.common_km, DecimalFormat("#.##").format(convertMetersToKilometers(if (this.isNaN()) 0F else this)))
        }
    }

    fun Int.addLengthUnit(): String {
        return if (this < 1000F) {
            getString(R.string.common_m, DecimalFormat("#").format(this))
        } else {
            getString(R.string.common_km, DecimalFormat("#.##").format(convertMetersToKilometers(this)))
        }
    }

    fun Double.conversionInKmH(): String {
        return getString(R.string.common_km_h, DecimalFormat("#.##").format(if (this.isNaN()) 0.0 else this))
    }

    fun Float.conversionInKmH(): String {
        return getString(R.string.common_km_h, DecimalFormat("#.##").format(if (this.isNaN()) 0F else this))
    }

    fun isLocationPermissionGranted(): Boolean {
        return application?.let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } ?: throw IllegalStateException(
            "Application context in Utils not initialized.Please call method init in your Application instance"
        )
    }
}

fun Context.getFormattedDistance(distance: Float): String {
    return if (distance < 1000F) {
        getString(R.string.common_m, DecimalFormat("#.##").format(getNumberOrZero(distance)))
    } else {
        getString(R.string.common_km, DecimalFormat("#.##").format(convertMetersToKilometers(getNumberOrZero(distance))))
    }
}

fun convertMetersToKilometers(meter: Float): Float {
    return meter / 1000
}

fun convertMetersToKilometers(meter: Int): Float {
    return meter.toFloat() / 1000
}

fun getNumberOrZero(value: Double): Double {
    return if (value.isNaN()) 0.0 else value
}

fun getNumberOrZero(value: Float): Float {
    return if (value.isNaN()) 0F else value
}

fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.getFormattedTime(millis: Long): String {
    return getString(
        R.string.common_format_time,
        TimeUnit.MILLISECONDS.toHours(millis),
        TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
    )
}