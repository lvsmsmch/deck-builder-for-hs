package com.lvsmsmch.deckbuilder.domain.common

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Loaded<T>(val data: T) : UiState<T>()
    data class Failed(val throwable: Throwable) : UiState<Nothing>()
}
