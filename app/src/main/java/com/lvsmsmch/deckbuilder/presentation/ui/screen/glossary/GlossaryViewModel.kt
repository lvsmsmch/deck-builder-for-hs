package com.lvsmsmch.deckbuilder.presentation.ui.screen.glossary

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GlossaryViewModel : ViewModel() {

    private val _state = MutableStateFlow(GlossaryState(isMetadataReady = true))
    val state: StateFlow<GlossaryState> = _state.asStateFlow()

    fun setQuery(q: String) {
        _state.update { it.copy(query = q) }
    }
}
