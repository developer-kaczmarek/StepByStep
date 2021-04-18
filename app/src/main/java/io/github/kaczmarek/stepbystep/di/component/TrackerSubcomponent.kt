package io.github.kaczmarek.stepbystep.di.component

import dagger.Subcomponent
import io.github.kaczmarek.stepbystep.di.module.TrackerModule
import io.github.kaczmarek.stepbystep.di.scope.TrackerScope
import io.github.kaczmarek.stepbystep.ui.tracker.TrackerPresenter

@TrackerScope
@Subcomponent(modules = [TrackerModule::class])
interface TrackerSubcomponent {
    fun inject(presenter: TrackerPresenter)
}