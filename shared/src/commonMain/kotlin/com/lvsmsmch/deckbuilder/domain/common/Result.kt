package com.lvsmsmch.deckbuilder.domain.common

import kotlinx.coroutines.CancellationException

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val throwable: Throwable) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    fun getOrNull(): T? = (this as? Success)?.data
}

inline fun <T> runCatchingResult(block: () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (t: Throwable) {
        Result.Error(t)
    }
