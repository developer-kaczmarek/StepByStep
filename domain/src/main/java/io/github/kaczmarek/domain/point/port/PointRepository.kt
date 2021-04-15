package io.github.kaczmarek.domain.point.port

import io.github.kaczmarek.domain.point.entity.PointEntity

interface PointRepository {
    suspend fun getPoints(): List<PointEntity>
    suspend fun deleteAll()
    suspend fun saveCurrentPoint(point: PointEntity)
}