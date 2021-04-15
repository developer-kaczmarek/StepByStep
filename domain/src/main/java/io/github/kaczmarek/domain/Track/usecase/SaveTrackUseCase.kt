package io.github.kaczmarek.domain.track.usecase

import io.github.kaczmarek.domain.track.entity.TrackEntity
import io.github.kaczmarek.domain.track.port.TrackRepository

class SaveTrackUseCase(private val repository: TrackRepository) {
    suspend fun execute(track: TrackEntity) {
        repository.saveTrack(track)
    }
}