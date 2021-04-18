package io.github.kaczmarek.cache.room.mapper

import io.github.kaczmarek.cache.room.model.PointDBModel
import io.github.kaczmarek.domain.base.Mapper
import io.github.kaczmarek.domain.point.entity.PointEntity

class PointDBModelMapper : Mapper<PointDBModel, PointEntity> {
    override fun mapToEntity(obj: PointDBModel): PointEntity {
        return PointEntity(obj.latitude, obj.longitude, obj.altitude, obj.accuracy, obj.speed)
    }

    override fun mapFromEntity(obj: PointEntity): PointDBModel {
        return PointDBModel(obj.latitude, obj.longitude, obj.altitude, obj.accuracy, obj.speed)
    }
}