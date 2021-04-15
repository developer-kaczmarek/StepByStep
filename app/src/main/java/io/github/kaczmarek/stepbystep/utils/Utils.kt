package io.github.kaczmarek.stepbystep.utils

import android.app.Application
import androidx.annotation.StringRes
import io.github.kaczmarek.stepbystep.R
import java.text.DecimalFormat

object Utils {
    private var application: Application? = null

    fun init(application: Application) {
        Utils.application = application
    }

    fun getString(@StringRes id: Int, vararg parameters: Any): String {
        return application?.getString(id, *parameters)
                ?: throw IllegalStateException(
                        "Application context in Utils not initialized.Please " +
                                "call method init in your Application instance"
                )
    }

    fun getKilometer(meter: Float): Float {
        return meter / 1000
    }

    fun getKilometer(meter: Int): Float {
        return meter.toFloat() / 1000
    }

    fun Float.addLengthUnit(): String {
        return if (this < 1000F) {
            getString(R.string.common_m, DecimalFormat("#.##").format(this))
        } else {
            getString(R.string.common_km, DecimalFormat("#.##").format(getKilometer(this)))
        }
    }

    fun Int.addLengthUnit(): String {
        return if (this < 1000F) {
            getString(R.string.common_m, DecimalFormat("#").format(this))
        } else {
            getString(R.string.common_km, DecimalFormat("#.##").format(getKilometer(this)))
        }
    }

    fun Double.conversionInKmH(): String {
        return getString(R.string.common_km_h, DecimalFormat("#.##").format(this))
    }

    fun Float.conversionInKmH(): String {
        return getString(R.string.common_km_h, DecimalFormat("#.##").format(this))
    }
}