package com.lvsmsmch.deckbuilder.presentation.ui.screen.battlegrounds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.usecases.SearchCardsUseCase
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

class BattlegroundsViewModel(
    private val searchCards: SearchCardsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BattlegroundsState())
    val state: StateFlow<BattlegroundsState> = _state.asStateFlow()

    private var inFlight: Job? = null

    init {
        // Heroes tab is implicitly tier=hero; everything else is tier-csv plus minionType.
        loadFirstPage()

        _state
            .map { it.textQuery }
            .distinctUntilChanged()
            .drop(1)
            .debounce(350L)
            .onEach { loadFirstPage() }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: BgTab) {
        if (tab == _state.value.tab) return
        _state.update {
            it.copy(
                tab = tab,
                // Switching tabs resets the tier scope to keep filters legible.
                tiers = emptySet(),
                minionTypes = emptySet(),
            )
        }
        loadFirstPage()
    }

    fun toggleTier(tier: String) {
        _state.update {
            val next = if (tier in it.tiers) it.tiers - tier else it.tiers + tier
            it.copy(tiers = next)
        }
        loadFirstPage()
    }

    fun toggleMinionType(slug: String) {
        _state.update {
            val next = if (slug in it.minionTypes) it.minionTypes - slug else it.minionTypes + slug
            it.copy(minionTypes = next)
        }
        loadFirstPage()
    }

    fun setQuery(q: String) {
        _state.update { it.copy(textQuery = q) }
    }

    fun loadNextPage() {
        val s = _state.value
        if (!s.hasMore || s.isLoadingMore || s.isLoadingFirstPage) return
        runSearch(targetPage = s.page + 1, replace = false)
    }

    fun retry() = loadFirstPage()

    private fun loadFirstPage() = runSearch(targetPage = 1, replace = true)

    private fun runSearch(targetPage: Int, replace: Boolean) {
        inFlight?.cancel()
        _state.update {
            it.copy(
                isLoadingFirstPage = replace,
                isLoadingMore = !replace,
                errorMessage = null,
            )
        }
        inFlight = viewModelScope.launch {
            val s = _state.value
            val tiers = when (s.tab) {
                BgTab.Heroes -> setOf("hero")
                BgTab.Minions -> s.tiers // empty = all tiers 1..6
            }
            val filters = CardFilters(
                gameMode = CardFilters.GameMode.BATTLEGROUNDS,
                tiers = tiers,
                minionTypes = s.minionTypes,
                textQuery = s.textQuery,
                collectibleOnly = false,
            )
            when (val r = searchCards(filters = filters, page = targetPage, pageSize = 60)) {
                is Result.Success -> _state.update { prev ->
                    val merged = if (replace) r.data.items else prev.cards + r.data.items
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
}
