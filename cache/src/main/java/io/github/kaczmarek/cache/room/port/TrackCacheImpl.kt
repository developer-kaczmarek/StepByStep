package io.github.kaczmarek.cache.room.port

import io.github.kaczmarek.cache.room.RoomDatabase
import io.github.kaczmarek.cache.room.mapper.TrackDBModelMapper
import io.github.kaczmarek.data.track.TrackCache
import io.github.kaczmarek.domain.track.entity.TrackEntity

class TrackCacheImpl(
    private val roomDatabase: RoomDatabase,
    private val mapper: TrackDBModelMapper
) : TrackCache {

    override suspend fun getTracks(): List<TrackEntity> {
        return roomDatabase.trackDao().getTracks().map { mapper.mapToEntity(it) }
    }

    override suspend fun getLastUnfinishedTrack(): TrackEntity? {
        val track = roomDatabase.trackDao().getUnfinishedTrack()
        return track?.let { mapper.mapToEntity(it) }
    }

    override suspend fun getTrackCount(): Int {
        return roomDatabase.trackDao().getTrackCount()
    }

    override suspend fun insert(vararg obj: TrackEntity) {
        roomDatabase.trackDao().insert(*obj.map { mapper.mapFromEntity(it) }.toTypedArray())
    }

    override suspend fun update(vararg obj: TrackEntity) {
        roomDatabase.trackDao().update(*obj.map { mapper.mapFromEntity(it) }.toTypedArray())
    }

    override suspend fun delete(vararg obj: TrackEntity) {
        roomDatabase.trackDao().delete(*obj.map { mapper.mapFromEntity(it) }.toTypedArray())
    }

    override suspend fun deleteAll() {
        roomDatabase.trackDao().deleteAll()
    }
}