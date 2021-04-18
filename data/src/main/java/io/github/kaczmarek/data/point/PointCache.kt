package io.github.kaczmarek.data.point

import io.github.kaczmarek.data.base.Cache
import io.github.kaczmarek.domain.point.entity.PointEntity

interface PointCache : Cache<PointEntity> {
    suspend fun getPoints(): List<PointEntity>
}