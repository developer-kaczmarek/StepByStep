package io.github.kaczmarek.stepbystep.di.component

import dagger.Subcomponent
import io.github.kaczmarek.stepbystep.di.module.TrackerServiceModule
import io.github.kaczmarek.stepbystep.di.scope.TrackerServiceScope
import io.github.kaczmarek.stepbystep.services.TrackerService

@TrackerServiceScope
@Subcomponent(modules = [TrackerServiceModule::class])
interface TrackerServiceSubcomponent {
    fun inject(service: TrackerService)
}