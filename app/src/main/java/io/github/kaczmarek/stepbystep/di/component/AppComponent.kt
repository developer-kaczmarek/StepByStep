package io.github.kaczmarek.stepbystep.di.component

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import io.github.kaczmarek.stepbystep.di.module.AppModule
import io.github.kaczmarek.stepbystep.di.module.LocationServiceModule
import io.github.kaczmarek.stepbystep.di.module.TrackerModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun addLocationServiceSubcomponent(module: LocationServiceModule): LocationServiceSubcomponent
    fun addTrackerSubcomponent(module: TrackerModule): TrackerSubcomponent

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}