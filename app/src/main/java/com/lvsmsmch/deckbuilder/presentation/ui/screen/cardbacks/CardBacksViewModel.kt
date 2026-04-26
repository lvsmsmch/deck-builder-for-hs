package com.lvsmsmch.deckbuilder.presentation.ui.screen.cardbacks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.CardBack
import com.lvsmsmch.deckbuilder.domain.usecases.SearchCardBacksUseCase
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

class CardBacksViewModel(
    private val searchCardBacks: SearchCardBacksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CardBacksState())
    val state: StateFlow<CardBacksState> = _state.asStateFlow()

    private var inFlight: Job? = null

    init {
        loadFirstPage()
        _state
            .map { it.textQuery }
            .distinctUntilChanged()
            .drop(1)
            .debounce(350L)
            .onEach { loadFirstPage() }
            .launchIn(viewModelScope)
    }

    fun setCategory(category: String?) {
        if (category == _state.value.category) return
        _state.update { it.copy(category = category) }
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

    fun select(item: CardBack?) {
        _state.update { it.copy(selected = item) }
    }

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
            when (val r = searchCardBacks(s.category, s.textQuery, targetPage)) {
                is Result.Success -> _state.update { prev ->
                    val merged = if (replace) r.data.items else prev.items + r.data.items
                    prev.copy(
                        items = merged,
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
