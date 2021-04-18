package io.github.kaczmarek.stepbystep.di.module

import dagger.Module
import dagger.Provides
import io.github.kaczmarek.cache.room.RoomDatabase
import io.github.kaczmarek.cache.room.mapper.PointDBModelMapper
import io.github.kaczmarek.cache.room.port.PointCacheImpl
import io.github.kaczmarek.data.point.PointCache
import io.github.kaczmarek.data.point.PointRepositoryImpl
import io.github.kaczmarek.domain.point.port.PointRepository
import io.github.kaczmarek.domain.point.usecase.SavePointUseCase
import io.github.kaczmarek.stepbystep.di.scope.LocationServiceScope

@Module
object LocationServiceModule {

    @Provides
    @LocationServiceScope
    fun providePointDBModelMapper(): PointDBModelMapper {
        return PointDBModelMapper()
    }

    @Provides
    @LocationServiceScope
    fun providePointCache(
        roomDatabase: RoomDatabase,
        mapper: PointDBModelMapper
    ): PointCache {
        return PointCacheImpl(roomDatabase, mapper)
    }

    @Provides
    @LocationServiceScope
    fun providePointRepository(pointCache: PointCache): PointRepository {
        return PointRepositoryImpl(pointCache)
    }

    @Provides
    @LocationServiceScope
    fun provideSavePointUseCase(repository: PointRepository): SavePointUseCase {
        return SavePointUseCase(repository)
    }
}