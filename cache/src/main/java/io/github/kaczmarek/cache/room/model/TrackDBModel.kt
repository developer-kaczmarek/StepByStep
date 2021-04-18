package io.github.kaczmarek.cache.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track")
class TrackDBModel(
        @PrimaryKey
        val id: String,
        val distance: Float,
        val recordTime: Long,
        val maxSpeed: Float,
        val averageSpeed: Double,
        val currentSpeed: Float,
        val isFinishedRecord: Boolean
)
