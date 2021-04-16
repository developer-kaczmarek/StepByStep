package io.github.kaczmarek.stepbystep.ui.main

interface LocationServiceLifecycleListener {
    fun startService()
    fun stopService()
    fun isServiceRunning(): Boolean
}