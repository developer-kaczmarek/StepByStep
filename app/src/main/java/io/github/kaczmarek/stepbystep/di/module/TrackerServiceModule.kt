package io.github.kaczmarek.stepbystep.di.module

import dagger.Module
import dagger.Provides
import io.github.kaczmarek.cache.room.RoomDatabase
import io.github.kaczmarek.cache.room.mapper.PointDBModelMapper
import io.github.kaczmarek.cache.room.mapper.TrackDBModelMapper
import io.github.kaczmarek.cache.room.port.PointCacheImpl
import io.github.kaczmarek.cache.room.port.TrackCacheImpl
import io.github.kaczmarek.data.point.PointCache
import io.github.kaczmarek.data.point.PointRepositoryImpl
import io.github.kaczmarek.data.track.TrackCache
import io.github.kaczmarek.data.track.TrackRepositoryImpl
import io.github.kaczmarek.domain.point.port.PointRepository
import io.github.kaczmarek.domain.point.usecase.SavePointUseCase
import io.github.kaczmarek.domain.track.port.TrackRepository
import io.github.kaczmarek.domain.track.usecase.GetLastUnfinishedTrackUseCase
import io.github.kaczmarek.stepbystep.di.scope.TrackerServiceScope

@Module
object TrackerServiceModule {

    @Provides
    @TrackerServiceScope
    fun providePointDBModelMapper(): PointDBModelMapper {
        return PointDBModelMapper()
    }

    @Provides
    @TrackerServiceScope
    fun providePointCache(
        roomDatabase: RoomDatabase,
        mapper: PointDBModelMapper
    ): PointCache {
        return PointCacheImpl(roomDatabase, mapper)
    }

    @Provides
    @TrackerServiceScope
    fun providePointRepository(pointCache: PointCache): PointRepository {
        return PointRepositoryImpl(pointCache)
    }

    @Provides
    @TrackerServiceScope
    fun provideSavePointUseCase(repository: PointRepository): SavePointUseCase {
        return SavePointUseCase(repository)
    }
    @Provides
    @TrackerServiceScope
    fun provideTrackDBModelMapper(): TrackDBModelMapper {
        return TrackDBModelMapper()
    }

    @Provides
    @TrackerServiceScope
    fun provideTrackCache(roomDatabase: RoomDatabase, mapper: TrackDBModelMapper): TrackCache {
        return TrackCacheImpl(roomDatabase, mapper)
    }

    @Provides
    @TrackerServiceScope
    fun provideTrackRepository(trackCache: TrackCache): TrackRepository {
        return TrackRepositoryImpl(trackCache)
    }

    @Provides
    @TrackerServiceScope
    fun provideGetLastUnfinishedTrackUseCase(repository: TrackRepository): GetLastUnfinishedTrackUseCase {
        return GetLastUnfinishedTrackUseCase(repository)
    }
}