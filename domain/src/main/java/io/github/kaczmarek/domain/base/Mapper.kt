package io.github.kaczmarek.domain.base

interface Mapper<T, D> {
    fun mapToEntity(obj: T): D

    fun mapFromEntity(obj: D): T
}