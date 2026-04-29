package com.lvsmsmch.deckbuilder.data.update

import com.lvsmsmch.deckbuilder.domain.entities.RotationStatus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface UpdateEvent {
    data class CardsUpdated(val build: String) : UpdateEvent
    data class RotationUpdated(val sha: String?) : UpdateEvent
}

/**
 * App-wide bridge for update outcomes. Lives as a Koin singleton so any screen
 * (snackbar host, library banner) can observe without wiring through ViewModels.
 */
class UpdateNotifier {
    private val _events = MutableSharedFlow<UpdateEvent>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<UpdateEvent> = _events.asSharedFlow()

    private val _rotationStatus = MutableStateFlow<RotationStatus?>(null)
    val rotationStatus: StateFlow<RotationStatus?> = _rotationStatus.asStateFlow()

    fun emit(event: UpdateEvent) {
        _events.tryEmit(event)
    }

    fun setRotationStatus(status: RotationStatus?) {
        _rotationStatus.value = status
    }
}
