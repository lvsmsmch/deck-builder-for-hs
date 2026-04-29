package com.lvsmsmch.deckbuilder.presentation.ui.screen.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository
import com.lvsmsmch.deckbuilder.domain.usecases.AssembleDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SaveDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SearchCardsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeckBuilderViewModel(
    private val searchCards: SearchCardsUseCase,
    private val assembleDeck: AssembleDeckUseCase,
    private val saveDeck: SaveDeckUseCase,
    private val rotation: RotationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BuilderState())
    val state: StateFlow<BuilderState> = _state.asStateFlow()

    private val _effects = Channel<BuilderEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var poolJob: Job? = null
    private var saveJob: Job? = null
    private var pickJob: Job? = null

    init {
        // Debounce pool search.
        _state
            .map { it.pool.textQuery }
            .distinctUntilChanged()
            .drop(1)
            .debounce(350L)
            .onEach { reloadPoolFirstPage() }
            .launchIn(viewModelScope)
    }

    /**
     * Resolve the canonical hero card for [slug] from the HsJson pool, then
     * commit the picked class and start the first pool fetch.
     */
    fun pickClassBySlug(slug: String) {
        pickJob?.cancel()
        pickJob = viewModelScope.launch {
            val heroFilters = CardFilters(
                classes = setOf(slug),
                types = setOf("hero"),
                collectibleOnly = true,
            )
            val r = searchCards(filters = heroFilters, page = 1, pageSize = 1)
            val hero: Card? = (r as? Result.Success)?.data?.items?.firstOrNull()
            val meta = hero?.classes?.firstOrNull { it.slug.equals(slug, ignoreCase = true) }
                ?: hero?.classes?.firstOrNull()
                ?: ClassMeta(id = 0, slug = slug, name = slug)
            _state.update {
                it.copy(
                    phase = Phase.Editing,
                    chosenClass = meta,
                    heroCardId = hero?.id,
                    deck = emptyMap(),
                    pool = PoolState(),
                    saveError = null,
                )
            }
            reloadPoolFirstPage()
        }
    }

    fun backToPicker() {
        poolJob?.cancel()
        pickJob?.cancel()
        _state.update {
            it.copy(
                phase = Phase.ClassPicker,
                chosenClass = null,
                heroCardId = null,
                deck = emptyMap(),
                pool = PoolState(),
                saveError = null,
            )
        }
    }

    fun setPoolQuery(query: String) {
        _state.update { it.copy(pool = it.pool.copy(textQuery = query)) }
    }

    fun loadNextPoolPage() {
        val pool = _state.value.pool
        if (!pool.hasMore || pool.isLoadingMore || pool.isLoading) return
        runPoolFetch(targetPage = pool.page + 1, replace = false)
    }

    fun addCard(card: Card, count: Int = 1) {
        val st = _state.value
        val clsSlug = st.chosenClass?.slug
        if (clsSlug != null && !card.fitsClass(clsSlug)) {
            flashToast("Not a $clsSlug or Neutral card")
            return
        }
        val existingCount = st.deck[card.id]?.count ?: 0
        val cap = when {
            st.singleton -> 1
            card.isLegendary() -> 1
            else -> 2
        }
        val target = (existingCount + count).coerceAtMost(cap)
        if (target == existingCount) {
            val msg = when {
                st.singleton -> "Highlander mode (×1)"
                card.isLegendary() -> "Legendary limit (×1)"
                else -> "Card limit (×2)"
            }
            flashToast(msg)
            return
        }
        if (st.cardCount + (target - existingCount) > st.maxDeckSize) {
            flashToast("Deck is full (${st.maxDeckSize}/${st.maxDeckSize})")
            return
        }
        _state.update {
            it.copy(deck = it.deck + (card.id to DeckCardEntry(card, target)))
        }
    }

    fun removeCard(card: Card) {
        val current = _state.value.deck[card.id] ?: return
        val nextCount = current.count - 1
        _state.update {
            it.copy(
                deck = if (nextCount <= 0) it.deck - card.id
                else it.deck + (card.id to current.copy(count = nextCount)),
            )
        }
    }

    fun clearDeck() {
        _state.update { it.copy(deck = emptyMap()) }
    }

    fun toggleHighlanderSize() {
        _state.update { it.copy(maxDeckSize = if (it.maxDeckSize == 30) 40 else 30) }
    }

    fun toggleSingleton() {
        _state.update {
            val next = !it.singleton
            val deck = if (next) it.deck.mapValues { (_, e) -> e.copy(count = 1) } else it.deck
            it.copy(singleton = next, deck = deck)
        }
    }

    fun setFormat(format: GameFormat) {
        if (format == _state.value.format) return
        _state.update { it.copy(format = format) }
        reloadPoolFirstPage()
    }

    fun dismissToast() {
        _state.update { it.copy(toast = null) }
    }

    fun save() {
        val st = _state.value
        if (!st.canSave) return
        saveJob?.cancel()
        _state.update { it.copy(isSaving = true, saveError = null) }
        saveJob = viewModelScope.launch {
            val ids = st.deckEntries.flatMap { entry ->
                List(entry.count) { entry.card.id }
            }
            when (val r = assembleDeck(ids = ids, heroCardId = st.heroCardId)) {
                is Result.Success -> {
                    val deck = r.data.copy(format = st.format)
                    saveDeck(deck, name = null)
                    _state.update { it.copy(isSaving = false) }
                    _effects.trySend(BuilderEffect.DeckSaved(deck.code))
                }
                is Result.Error -> _state.update {
                    it.copy(
                        isSaving = false,
                        saveError = r.throwable.message ?: r.throwable.javaClass.simpleName,
                    )
                }
            }
        }
    }

    private fun reloadPoolFirstPage() {
        if (_state.value.chosenClass == null) return
        runPoolFetch(targetPage = 1, replace = true)
    }

    private fun runPoolFetch(targetPage: Int, replace: Boolean) {
        poolJob?.cancel()
        _state.update {
            it.copy(
                pool = it.pool.copy(
                    isLoading = replace,
                    isLoadingMore = !replace,
                    errorMessage = null,
                ),
            )
        }
        val st = _state.value
        val clsSlug = st.chosenClass?.slug ?: return
        poolJob = viewModelScope.launch {
            val formatSets: Set<String> = when (st.format) {
                GameFormat.STANDARD -> rotation.cached()?.standardSets
                    ?.map { it.lowercase() }?.toSet().orEmpty()
                else -> emptySet()
            }
            val filters = CardFilters(
                classes = setOf(clsSlug),
                textQuery = st.pool.textQuery,
                collectibleOnly = true,
                sets = formatSets,
            )
            val neutralFilters = filters.copy(classes = setOf("neutral"))
            val classResult = searchCards(filters, page = targetPage)
            val neutralResult = searchCards(neutralFilters, page = targetPage)

            if (classResult is Result.Error) {
                _state.update {
                    it.copy(
                        pool = it.pool.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = classResult.throwable.message,
                        ),
                    )
                }
                return@launch
            }
            classResult as Result.Success
            val classPage = classResult.data
            val neutralPage = (neutralResult as? Result.Success)?.data

            val merged = (classPage.items + (neutralPage?.items ?: emptyList()))
                .sortedWith(compareBy({ it.manaCost }, { it.name }))

            _state.update {
                val newCards = if (replace) merged else it.pool.cards + merged
                val pageCount = maxOf(classPage.pageCount, neutralPage?.pageCount ?: 0)
                val totalCount = classPage.totalCount + (neutralPage?.totalCount ?: 0)
                it.copy(
                    pool = it.pool.copy(
                        cards = newCards,
                        page = classPage.pageNumber,
                        pageCount = pageCount,
                        totalCount = totalCount,
                        isLoading = false,
                        isLoadingMore = false,
                    ),
                )
            }
        }
    }

    private fun flashToast(message: String) {
        _state.update { it.copy(toast = message) }
    }

    private fun Card.fitsClass(chosenSlug: String): Boolean {
        if (classes.isEmpty()) return true
        return classes.any { it.slug.equals(chosenSlug, ignoreCase = true) || it.slug.equals("neutral", ignoreCase = true) }
    }

    private fun Card.isLegendary(): Boolean =
        rarity?.slug?.equals("legendary", ignoreCase = true) == true
}
