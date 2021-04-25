package io.github.kaczmarek.cache.room.dao

import androidx.room.Dao
import androidx.room.Query
import io.github.kaczmarek.cache.room.model.TrackDBModel

@Dao
interface TrackDao : BaseDao<TrackDBModel> {

    @Query("SELECT * FROM track")
    suspend fun getTracks(): List<TrackDBModel>

    @Query("SELECT * FROM track WHERE isFinishedRecord = 0")
    suspend fun getUnfinishedTrack(): TrackDBModel?

    @Query("DELETE FROM track")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM track")
    suspend fun getTrackCount(): Int

}