package com.lvsmsmch.deckbuilder.presentation.ui.screen.glossary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.entities.Keyword
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class GlossaryViewModel(
    metadata: MetadataRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _state = MutableStateFlow(GlossaryState())
    val state: StateFlow<GlossaryState> = _state.asStateFlow()

    init {
        combine(metadata.current, _query) { meta, q -> meta to q.trim() }
            .map { (meta, q) -> regroup(meta, q) }
            .onEach { next -> _state.update { next } }
            .launchIn(viewModelScope)
    }

    fun setQuery(q: String) {
        _query.value = q
        _state.update { it.copy(query = q) }
    }

    private fun regroup(meta: Metadata?, q: String): GlossaryState {
        if (meta == null || meta.keywords.isEmpty()) {
            return GlossaryState(query = q, groups = emptyList(), isMetadataReady = false)
        }
        val all: Collection<Keyword> = meta.keywords.values
        val needle = q.lowercase()
        val filtered = if (needle.isEmpty()) {
            all.toList()
        } else {
            all.filter { kw ->
                kw.name.lowercase().contains(needle) ||
                    kw.refText.lowercase().contains(needle) ||
                    kw.slug.lowercase().contains(needle)
            }
        }
        val groups = filtered
            .sortedBy { it.name.lowercase() }
            .groupBy { it.name.firstOrNull()?.uppercase() ?: "—" }
            .toSortedMap()
            .map { (letter, items) -> Group(letter = letter, items = items) }
        return GlossaryState(query = q, groups = groups, isMetadataReady = true)
    }
}
