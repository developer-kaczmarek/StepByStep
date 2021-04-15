package io.github.kaczmarek.domain.point.usecase

import io.github.kaczmarek.domain.point.entity.PointEntity
import io.github.kaczmarek.domain.point.port.PointRepository

class GetPointsUseCase(private val repository: PointRepository) {
    suspend fun execute(): List<PointEntity> {
        return repository.getPoints()
    }
}