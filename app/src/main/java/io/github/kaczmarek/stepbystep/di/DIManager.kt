package io.github.kaczmarek.stepbystep.di

import io.github.kaczmarek.stepbystep.di.component.AppComponent
import io.github.kaczmarek.stepbystep.di.component.LocationServiceSubcomponent
import io.github.kaczmarek.stepbystep.di.component.TrackerSubcomponent
import io.github.kaczmarek.stepbystep.di.module.LocationServiceModule
import io.github.kaczmarek.stepbystep.di.module.TrackerModule

object DIManager {
    lateinit var appComponent: AppComponent

    private var locationServiceSubcomponent: LocationServiceSubcomponent? = null
    private var trackerSubcomponent: TrackerSubcomponent? = null

    fun getLocationServiceSubcomponent(): LocationServiceSubcomponent {
        if (locationServiceSubcomponent == null) {
            locationServiceSubcomponent =
                appComponent.addLocationServiceSubcomponent(LocationServiceModule)
        }
        return locationServiceSubcomponent
            ?: throw IllegalStateException("$locationServiceSubcomponent must not be null")
    }

    fun removeLocationServiceSubcomponent() {
        locationServiceSubcomponent = null
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