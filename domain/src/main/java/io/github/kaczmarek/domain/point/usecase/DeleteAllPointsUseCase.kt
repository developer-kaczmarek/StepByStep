package io.github.kaczmarek.domain.point.usecase

import io.github.kaczmarek.domain.point.port.PointRepository

class DeleteAllPointsUseCase(private val repository: PointRepository) {
    suspend fun execute() {
        repository.deleteAll()
    }
}