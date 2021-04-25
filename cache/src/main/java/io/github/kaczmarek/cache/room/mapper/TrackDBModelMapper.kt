package io.github.kaczmarek.cache.room.mapper

import io.github.kaczmarek.cache.room.model.TrackDBModel
import io.github.kaczmarek.domain.base.Mapper
import io.github.kaczmarek.domain.track.entity.TrackEntity

class TrackDBModelMapper : Mapper<TrackDBModel, TrackEntity> {
    override fun mapToEntity(obj: TrackDBModel): TrackEntity {
        return TrackEntity(obj.id, obj.distance, obj.duration, obj.maxSpeed, obj.averageSpeed, obj.currentSpeed, obj.isFinishedRecord)
    }

    override fun mapFromEntity(obj: TrackEntity): TrackDBModel {
        return TrackDBModel(obj.id, obj.distance, obj.duration, obj.maxSpeed, obj.averageSpeed, obj.currentSpeed, obj.isFinishedRecord)
    }
}