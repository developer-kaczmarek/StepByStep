package io.github.kaczmarek.domain.point.usecase

import io.github.kaczmarek.domain.point.entity.PointEntity
import io.github.kaczmarek.domain.point.port.PointRepository

class SavePointUseCase(private val repository: PointRepository) {
    suspend fun execute(point: PointEntity) {
        repository.saveCurrentPoint(point)
    }
}