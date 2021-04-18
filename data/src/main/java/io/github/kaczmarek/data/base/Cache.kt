package io.github.kaczmarek.data.base

interface Cache<T> {
    suspend fun insert(vararg obj: T)

    suspend fun update(vararg obj: T)

    suspend fun delete(vararg obj: T)

    suspend fun deleteAll()
}