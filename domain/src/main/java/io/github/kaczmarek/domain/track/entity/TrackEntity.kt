package io.github.kaczmarek.domain.track.entity

data class TrackEntity(
    val id: String, // Индетификатор трека
    val distance: Float? = null, // Пройденная дистанция
    val duration: Long, // Продолжительность записи
    val maxSpeed: Float? = null, // Максимальная скорость
    val averageSpeed: Double? = null, // Средняя скорость
    val currentSpeed: Float? = null, // Текущая скорость (последняя зафиксированная)
    val isFinishedRecord: Boolean // Флаг остановлена ли запись по текущему треку
)