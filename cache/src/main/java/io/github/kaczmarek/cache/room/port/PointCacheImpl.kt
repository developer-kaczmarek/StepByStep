package io.github.kaczmarek.cache.room.port

import io.github.kaczmarek.cache.room.RoomDatabase
import io.github.kaczmarek.cache.room.mapper.PointDBModelMapper
import io.github.kaczmarek.data.point.PointCache
import io.github.kaczmarek.domain.point.entity.PointEntity

class PointCacheImpl(
    private val roomDatabase: RoomDatabase,
    private val mapper: PointDBModelMapper
) : PointCache {

    override suspend fun getPoints(): List<PointEntity> {
        return roomDatabase.pointDao().getPoints().map { mapper.mapToEntity(it) }
    }

    override suspend fun insert(vararg obj: PointEntity) {
        roomDatabase.pointDao().insert(*obj.map { mapper.mapFromEntity(it) }.toTypedArray())
    }

    override suspend fun update(vararg obj: PointEntity) {
        roomDatabase.pointDao().update(*obj.map { mapper.mapFromEntity(it) }.toTypedArray())
    }

    override suspend fun delete(vararg obj: PointEntity) {
        roomDatabase.pointDao().delete(*obj.map { mapper.mapFromEntity(it) }.toTypedArray())
    }

    override suspend fun deleteAll() {
        roomDatabase.pointDao().deleteAll()
    }
}