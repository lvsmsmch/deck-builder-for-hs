package com.lvsmsmch.deckbuilder.presentation.ui.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
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

    fun toggleCollectibleOnly() {
        _state.update { it.copy(filters = it.filters.copy(collectibleOnly = !it.filters.collectibleOnly)) }
    }

    fun toggleManaCost(cost: Int) {
        _state.update {
            val current = it.filters.manaCosts
            val next = if (cost in current) current - cost else current + cost
            it.copy(filters = it.filters.copy(manaCosts = next))
        }
    }

    fun toggleClass(slug: String) {
        _state.update {
            val current = it.filters.classes
            val next = if (slug in current) current - slug else current + slug
            it.copy(filters = it.filters.copy(classes = next))
        }
    }

    fun applyFilters(filters: CardFilters) {
        if (filters == _state.value.filters) return
        _state.update { it.copy(filters = filters) }
    }

    fun resetFilters() {
        if (_state.value.filters == CardFilters()) return
        _state.update { it.copy(filters = CardFilters()) }
    }

    fun setSort(key: SortKey, direction: SortDir = SortDir.ASC) {
        val nextSort = CardSort(key = key, direction = direction)
        if (nextSort == _state.value.filters.sort) return
        _state.update { it.copy(filters = it.filters.copy(sort = nextSort)) }
    }

    fun loadNextPage() {
        val s = _state.value
        if (!s.hasMore || s.isLoadingMore || s.isLoadingFirstPage) return
        runSearch(targetPage = s.page + 1, replaceItems = false)
    }

    fun retry() = loadFirstPage()

    private fun loadFirstPage() = runSearch(targetPage = 1, replaceItems = true)

    private fun runSearch(targetPage: Int, replaceItems: Boolean) {
        inFlight?.cancel()
        _state.update {
            it.copy(
                isLoadingFirstPage = replaceItems,
                isLoadingMore = !replaceItems,
                errorMessage = null,
            )
        }
        inFlight = viewModelScope.launch {
            val filters = _state.value.filters
            val r = searchCards(filters = filters, page = targetPage)
            when (r) {
                is Result.Success -> _state.update { prev ->
                    val merged = if (replaceItems) r.data.items else prev.cards + r.data.items
                    prev.copy(
                        cards = merged,
                        page = r.data.pageNumber,
                        pageCount = r.data.pageCount,
                        totalCount = r.data.totalCount,
                        isLoadingFirstPage = false,
                        isLoadingMore = false,
                    )
                }

                is Result.Error -> _state.update {
                    it.copy(
                        isLoadingFirstPage = false,
                        isLoadingMore = false,
                        errorMessage = r.throwable.message ?: r.throwable.javaClass.simpleName,
                    )
                }
            }
        }
    }

    private companion object {
        const val FILTER_DEBOUNCE_MS = 200L
    }
}
