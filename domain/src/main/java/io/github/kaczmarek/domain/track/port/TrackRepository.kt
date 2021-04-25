package io.github.kaczmarek.domain.track.port

import io.github.kaczmarek.domain.track.entity.TrackEntity

interface TrackRepository {
    suspend fun saveTrack(track: TrackEntity)
    suspend fun getTracks(): List<TrackEntity>
    suspend fun getLastUnfinishedTrack(): TrackEntity?
    suspend fun deleteAll()
    suspend fun getTrackCount(): Int
}