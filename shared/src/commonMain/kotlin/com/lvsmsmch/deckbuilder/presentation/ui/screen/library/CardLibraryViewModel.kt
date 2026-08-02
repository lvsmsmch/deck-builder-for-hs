package com.lvsmsmch.deckbuilder.presentation.ui.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.domain.usecases.SearchCardsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CardLibraryViewModel(
    private val searchCards: SearchCardsUseCase,
    private val prefs: PreferencesRepository,
    initialKeyword: String? = null,
    initialSetSlug: String? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CardLibraryState(
            filters = CardFilters(
                keywords = listOfNotNull(initialKeyword?.takeIf { it.isNotBlank() }).toSet(),
                sets = listOfNotNull(initialSetSlug?.takeIf { it.isNotBlank() }).toSet(),
            ),
            isLoadingFirstPage = true,
        ),
    )
    val state: StateFlow<CardLibraryState> = _state.asStateFlow()

    private var inFlight: Job? = null

    init {
        // Re-fetch when the user flips Card language in Settings.
        prefs.preferences
            .map { it.cardLocale }
            .distinctUntilChanged()
            .drop(1)
            .onEach { loadFirstPage() }
            .launchIn(viewModelScope)

        // Single debounced pipeline for *all* filter changes.
        _state
            .map { it.filters }
            .distinctUntilChanged()
            .drop(1)
            .debounce(FILTER_DEBOUNCE_MS)
            .onEach { loadFirstPage() }
            .launchIn(viewModelScope)

        loadFirstPage()
    }

    fun setTextQuery(query: String) {
        _state.update { it.copy(filters = it.filters.copy(textQuery = query)) }
    }




    fun applyFilters(filters: CardFilters) {
        if (filters == _state.value.filters) return
        _state.update { it.copy(filters = filters) }
    }


    fun setSort(key: SortKey, direction: SortDir = SortDir.ASC) {
        val nextSort = CardSort(key = key, direction = direction)
        if (nextSort == _state.value.filters.sort) return
        _state.update { it.copy(filters = it.filters.copy(sort = nextSort)) }
    }

    fun loadNextPage() {
        val current = _state.value
        if (!current.canLoadMore) return
        runSearch(targetPage = current.page + 1, replaceItems = false)
    }

    fun retry() = loadFirstPage()

    private fun loadFirstPage() = runSearch(targetPage = 1, replaceItems = true)

    private fun runSearch(targetPage: Int, replaceItems: Boolean) {
        inFlight?.cancel()
        _state.update { it.onLoadStarted(replace = replaceItems) }
        inFlight = viewModelScope.launch {
            val result = searchCards(filters = _state.value.filters, page = targetPage)
            _state.update { it.onResult(result, replace = replaceItems) }
        }
    }


    private companion object {
        const val FILTER_DEBOUNCE_MS = 200L
    }
}
