package io.github.kaczmarek.data.track

import io.github.kaczmarek.data.base.Cache
import io.github.kaczmarek.domain.track.entity.TrackEntity

interface TrackCache : Cache<TrackEntity> {
    suspend fun getTracks(): List<TrackEntity>
    suspend fun getLastUnfinishedTrack(): TrackEntity?
    suspend fun getTrackCount(): Int
}