package com.lvsmsmch.deckbuilder.presentation.ui.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.domain.usecases.AcknowledgeNewSetUseCase
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
    private val metadata: MetadataRepository,
    private val prefs: PreferencesRepository,
    private val acknowledgeNewSet: AcknowledgeNewSetUseCase,
    initialKeyword: String? = null,
    initialSetSlug: String? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CardLibraryState(
            filters = CardFilters(
                keywords = listOfNotNull(initialKeyword?.takeIf { it.isNotBlank() }).toSet(),
                sets = listOfNotNull(initialSetSlug?.takeIf { it.isNotBlank() }).toSet(),
            ),
            // Spinner-by-default so the screen never flashes the empty-state
            // placeholder before the first search returns.
            isLoadingFirstPage = true,
        ),
    )
    val state: StateFlow<CardLibraryState> = _state.asStateFlow()

    private var inFlight: Job? = null

    init {
        // Mirror metadata into state so the filter sheet can render set/rarity lists.
        // Also recompute the new-set banner whenever metadata or pref changes.
        metadata.current
            .onEach { meta ->
                _state.update { it.copy(metadata = meta) }
                refreshNewSetBanner()
            }
            .launchIn(viewModelScope)
        prefs.preferences
            .map { it.lastSeenSetSlug }
            .distinctUntilChanged()
            .onEach { refreshNewSetBanner() }
            .launchIn(viewModelScope)

        // Re-fetch when the resolved card locale flips (Settings → Card language,
        // or first metadata arrival after a fresh install).
        metadata.current
            .map { it?.locale }
            .distinctUntilChanged()
            .drop(1)
            .onEach { loadFirstPage() }
            .launchIn(viewModelScope)

        // Single debounced pipeline for *all* filter changes (chips, sort, text,
        // FilterSheet apply). Cancels the in-flight request and runs once after
        // the user stops fiddling for FILTER_DEBOUNCE_MS. drop(1) skips the
        // synthetic initial emission — the first load is kicked off explicitly
        // below so we don't pay the debounce delay at cold start.
        _state
            .map { it.filters }
            .distinctUntilChanged()
            .drop(1)
            .debounce(FILTER_DEBOUNCE_MS)
            .onEach { loadFirstPage() }
            .launchIn(viewModelScope)

        // Kick off the first search immediately. CardRepositoryImpl falls back to
        // Metadata.Empty when metadata isn't loaded yet — cards still render, and
        // the locale-change handler above will refetch once metadata lands.
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

    fun openNewSetBanner() {
        val set = _state.value.newSetBanner ?: return
        viewModelScope.launch {
            acknowledgeNewSet(set.slug)
        }
        _state.update {
            it.copy(
                filters = it.filters.copy(sets = setOf(set.slug)),
                newSetBanner = null,
            )
        }
    }

    fun dismissNewSetBanner() {
        val set = _state.value.newSetBanner ?: return
        viewModelScope.launch { acknowledgeNewSet(set.slug) }
        _state.update { it.copy(newSetBanner = null) }
    }

    private suspend fun refreshNewSetBanner() {
        val meta = _state.value.metadata ?: return
        if (meta.sets.isEmpty()) return
        val lastSeen = prefs.current().lastSeenSetSlug
        val standardSlugs = meta.setGroups["standard"]?.cardSets?.toSet() ?: emptySet()
        val newest = meta.sets.values
            .filter { it.type.equals("expansion", ignoreCase = true) }
            .filter { standardSlugs.isEmpty() || it.slug in standardSlugs }
            .maxByOrNull { it.id }
            ?: return
        val showBanner = newest.slug != lastSeen
        _state.update { it.copy(newSetBanner = newest.takeIf { _ -> showBanner }) }
    }

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
