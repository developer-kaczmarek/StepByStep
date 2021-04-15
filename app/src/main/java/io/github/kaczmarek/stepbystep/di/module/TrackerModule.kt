package io.github.kaczmarek.stepbystep.di.module

import dagger.Module
import dagger.Provides
import io.github.kaczmarek.cache.room.RoomDatabase
import io.github.kaczmarek.cache.room.mapper.PointDBModelMapper
import io.github.kaczmarek.cache.room.port.PointCacheImpl
import io.github.kaczmarek.data.point.PointCache
import io.github.kaczmarek.data.point.PointRepositoryImpl
import io.github.kaczmarek.domain.point.port.PointRepository
import io.github.kaczmarek.domain.point.usecase.GetPointsUseCase
import io.github.kaczmarek.stepbystep.di.scope.TrackerScope

@Module
object TrackerModule {

    @Provides
    @TrackerScope
    fun providePointDBModelMapper(): PointDBModelMapper {
        return PointDBModelMapper()
    }

    @Provides
    @TrackerScope
    fun providePointCache(
        roomDatabase: RoomDatabase,
        mapper: PointDBModelMapper
    ): PointCache {
        return PointCacheImpl(roomDatabase, mapper)
    }

    @Provides
    @TrackerScope
    fun providePointRepository(pointCache: PointCache): PointRepository {
        return PointRepositoryImpl(pointCache)
    }

    @Provides
    @TrackerScope
    fun provideSavePointUseCase(repository: PointRepository): GetPointsUseCase {
        return GetPointsUseCase(repository)
    }
}