package io.github.kaczmarek.stepbystep.di.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import io.github.kaczmarek.cache.room.RoomDatabase
import io.github.kaczmarek.stepbystep.framework.STEP_BY_STEP_PREFS_NAME
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(STEP_BY_STEP_PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideRoomDatabase(context: Context, prefs: SharedPreferences): RoomDatabase {
        return RoomDatabase.getDatabase(context, prefs)
    }
}
