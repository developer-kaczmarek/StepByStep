package io.github.kaczmarek.stepbystep.di.component

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import io.github.kaczmarek.stepbystep.di.module.AppModule
import io.github.kaczmarek.stepbystep.di.module.TrackerServiceModule
import io.github.kaczmarek.stepbystep.di.module.TrackerModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun addTrackerServiceSubcomponent(module: TrackerServiceModule): TrackerServiceSubcomponent
    fun addTrackerSubcomponent(module: TrackerModule): TrackerSubcomponent

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}