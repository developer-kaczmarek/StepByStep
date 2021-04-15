package io.github.kaczmarek.stepbystep

import android.app.Application
import io.github.kaczmarek.stepbystep.di.DIManager
import io.github.kaczmarek.stepbystep.di.component.DaggerAppComponent

@Suppress("unused")
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DIManager.appComponent = DaggerAppComponent.builder()
                .context(this)
                .build()
    }
}