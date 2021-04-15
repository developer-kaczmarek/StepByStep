package io.github.kaczmarek.domain.track.entity

class TrackEntity(
    val distance: Float,
    val time: Long,
    val maxSpeed: Float,
    val averageSpeed: Float,
    val currentSpeed: Float
)