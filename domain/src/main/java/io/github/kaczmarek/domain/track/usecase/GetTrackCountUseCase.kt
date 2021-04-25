package io.github.kaczmarek.domain.track.usecase

import io.github.kaczmarek.domain.track.port.TrackRepository

class GetTrackCountUseCase(private val repository: TrackRepository) {
    suspend fun execute(): Int {
        return repository.getTrackCount()
    }
}