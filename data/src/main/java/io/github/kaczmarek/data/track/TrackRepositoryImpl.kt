package io.github.kaczmarek.data.track

import io.github.kaczmarek.domain.track.entity.TrackEntity
import io.github.kaczmarek.domain.track.port.TrackRepository

class TrackRepositoryImpl(private val trackCache: TrackCache) : TrackRepository {

    override suspend fun saveTrack(track: TrackEntity) {
        trackCache.insert(track)
    }

    override suspend fun getTracks(): List<TrackEntity> {
        return trackCache.getTracks()
    }

    override suspend fun getUnfinishedTrack(): TrackEntity? {
        return trackCache.getUnfinishedTrack()
    }

    override suspend fun deleteAll() {
        trackCache.deleteAll()
    }

    override suspend fun getTrackCount(): Int {
        return trackCache.getTrackCount()
    }
}