package io.github.kaczmarek.cache.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point")
class PointDBModel(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val speed: Float
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
