package io.github.kaczmarek.cache.room

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Database
import androidx.room.Room
import io.github.kaczmarek.cache.room.dao.PointDao
import io.github.kaczmarek.cache.room.dao.TrackDao
import io.github.kaczmarek.cache.room.model.PointDBModel
import io.github.kaczmarek.cache.room.model.TrackDBModel

@Database(
    entities = [
        PointDBModel::class,
        TrackDBModel::class
    ],
    version = 1
)
abstract class RoomDatabase : androidx.room.RoomDatabase() {

    abstract fun pointDao(): PointDao
    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var INSTANCE: RoomDatabase? = null
        private const val DB_NAME = "step_by_step_db"
        private lateinit var prefs: SharedPreferences

        fun getDatabase(context: Context, prefs: SharedPreferences): RoomDatabase {
            Companion.prefs = prefs

            val tempInstance = INSTANCE
            tempInstance?.let { return it }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context, RoomDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}