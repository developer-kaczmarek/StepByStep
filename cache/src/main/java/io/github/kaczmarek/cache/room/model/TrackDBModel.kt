package io.github.kaczmarek.cache.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track")
class TrackDBModel(
        @PrimaryKey
        val id: String, // Индетификатор трека
        val distance: Double?, // Пройденная дистанция
        val duration: Long, // Продолжительность записи
        val maxSpeed: Double?, // Максимальная скорость
        val averageSpeed: Double?, // Средняя скорость
        val currentSpeed: Double?, // Текущая скорость (последняя зафиксированная)
        val isFinishedRecord: Boolean // Флаг остановлена ли запись по текущему треку
)
