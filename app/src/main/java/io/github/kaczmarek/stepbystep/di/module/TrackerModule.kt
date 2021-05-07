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
import io.github.kaczmarek.domain.point.usecase.DeleteAllPointsUseCase
import io.github.kaczmarek.domain.point.usecase.GetPointsUseCase
import io.github.kaczmarek.domain.track.port.TrackRepository
import io.github.kaczmarek.domain.track.usecase.GetLastUnfinishedTrackUseCase
import io.github.kaczmarek.domain.track.usecase.GetTrackCountUseCase
import io.github.kaczmarek.domain.track.usecase.SaveTrackUseCase
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
    fun providePointCache(roomDatabase: RoomDatabase, mapper: PointDBModelMapper): PointCache {
        return PointCacheImpl(roomDatabase, mapper)
    }

    @Provides
    @TrackerScope
    fun providePointRepository(pointCache: PointCache): PointRepository {
        return PointRepositoryImpl(pointCache)
    }

    @Provides
    @TrackerScope
    fun provideGetPointsUseCase(repository: PointRepository): GetPointsUseCase {
        return GetPointsUseCase(repository)
    }

    @Provides
    @TrackerScope
    fun provideDeleteAllPointsUseCase(repository: PointRepository): DeleteAllPointsUseCase {
        return DeleteAllPointsUseCase(repository)
    }

    @Provides
    @TrackerScope
    fun provideTrackDBModelMapper(): TrackDBModelMapper {
        return TrackDBModelMapper()
    }

    @Provides
    @TrackerScope
    fun provideTrackCache(roomDatabase: RoomDatabase, mapper: TrackDBModelMapper): TrackCache {
        return TrackCacheImpl(roomDatabase, mapper)
    }

    @Provides
    @TrackerScope
    fun provideTrackRepository(trackCache: TrackCache): TrackRepository {
        return TrackRepositoryImpl(trackCache)
    }

    @Provides
    @TrackerScope
    fun provideSaveTrackUseCase(repository: TrackRepository): SaveTrackUseCase {
        return SaveTrackUseCase(repository)
    }

    @Provides
    @TrackerScope
    fun provideGetTrackCountUseCase(repository: TrackRepository): GetTrackCountUseCase {
        return GetTrackCountUseCase(repository)
    }

    @Provides
    @TrackerScope
    fun provideGetLastUnfinishedTrackUseCase(repository: TrackRepository): GetLastUnfinishedTrackUseCase {
        return GetLastUnfinishedTrackUseCase(repository)
    }
}