package com.lvsmsmch.deckbuilder.presentation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * App-wide snackbar channel. One host (in the nav graph) renders everything,
 * so every confirmation looks and behaves the same on both platforms — this
 * replaces the mix of platform toasts and per-screen snackbar hosts.
 */
class SnackbarController {

    data class Message(
        val text: UiText,
        val actionLabel: UiText? = null,
        val onAction: (() -> Unit)? = null,
    )

    private val _messages = MutableSharedFlow<Message>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val messages: SharedFlow<Message> = _messages.asSharedFlow()

    fun show(text: UiText) {
        _messages.tryEmit(Message(text))
    }

    fun show(text: UiText, actionLabel: UiText, onAction: () -> Unit) {
        _messages.tryEmit(Message(text, actionLabel, onAction))
    }
}
