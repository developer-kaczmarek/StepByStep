package io.github.kaczmarek.data.point

import io.github.kaczmarek.domain.point.entity.PointEntity
import io.github.kaczmarek.domain.point.port.PointRepository

class PointRepositoryImpl(private val pointCache: PointCache) : PointRepository {

    override suspend fun getPoints(): List<PointEntity> {
        return pointCache.getPoints()
    }

    override suspend fun deleteAll() {
        pointCache.deleteAll()
    }

    override suspend fun saveCurrentPoint(point: PointEntity) {
        pointCache.insert(point)
    }
}