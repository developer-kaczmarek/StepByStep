package io.github.kaczmarek.domain.track.port

import io.github.kaczmarek.domain.track.entity.TrackEntity

interface TrackRepository {
    suspend fun saveTrack(track: TrackEntity)
}