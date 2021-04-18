package io.github.kaczmarek.domain.track.entity

class TrackEntity(
    val id: String,
    val distance: Float,
    val recordTime: Long,
    val maxSpeed: Float,
    val averageSpeed: Double,
    val currentSpeed: Float,
    val isFinishedRecord: Boolean
)