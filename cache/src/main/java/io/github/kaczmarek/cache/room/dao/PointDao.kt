package io.github.kaczmarek.cache.room.dao

import androidx.room.Dao
import androidx.room.Query
import io.github.kaczmarek.cache.room.model.PointDBModel

@Dao
interface PointDao : BaseDao<PointDBModel> {

    @Query("SELECT * FROM point")
    suspend fun getPoints(): List<PointDBModel>

    @Query("DELETE FROM point")
    suspend fun deleteAll()

}