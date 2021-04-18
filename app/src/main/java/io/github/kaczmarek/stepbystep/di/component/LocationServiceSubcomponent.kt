package io.github.kaczmarek.stepbystep.di.component

import dagger.Subcomponent
import io.github.kaczmarek.stepbystep.di.module.LocationServiceModule
import io.github.kaczmarek.stepbystep.di.scope.LocationServiceScope
import io.github.kaczmarek.stepbystep.services.LocationService

@LocationServiceScope
@Subcomponent(modules = [LocationServiceModule::class])
interface LocationServiceSubcomponent {
    fun inject(service: LocationService)
}