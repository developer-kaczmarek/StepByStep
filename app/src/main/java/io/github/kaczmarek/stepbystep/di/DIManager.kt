package io.github.kaczmarek.stepbystep.di

import io.github.kaczmarek.stepbystep.di.component.AppComponent
import io.github.kaczmarek.stepbystep.di.component.TrackerServiceSubcomponent
import io.github.kaczmarek.stepbystep.di.component.TrackerSubcomponent
import io.github.kaczmarek.stepbystep.di.module.TrackerServiceModule
import io.github.kaczmarek.stepbystep.di.module.TrackerModule

object DIManager {
    lateinit var appComponent: AppComponent

    private var trackerServiceSubcomponent: TrackerServiceSubcomponent? = null
    private var trackerSubcomponent: TrackerSubcomponent? = null

    fun getTrackerServiceSubcomponent(): TrackerServiceSubcomponent {
        if (trackerServiceSubcomponent == null) {
            trackerServiceSubcomponent =
                appComponent.addTrackerServiceSubcomponent(TrackerServiceModule)
        }
        return trackerServiceSubcomponent
            ?: throw IllegalStateException("$trackerServiceSubcomponent must not be null")
    }

    fun removeTrackerServiceSubcomponent() {
        trackerServiceSubcomponent = null
    }

    fun getTrackerSubcomponent(): TrackerSubcomponent {
        if (trackerSubcomponent == null) {
            trackerSubcomponent = appComponent.addTrackerSubcomponent(TrackerModule)
        }
        return trackerSubcomponent
            ?: throw IllegalStateException("$trackerSubcomponent must not be null")
    }

    fun removeTrackerSubcomponent() {
        trackerSubcomponent = null
    }
}