package com.lvsmsmch.deckbuilder.presentation.ui.screen.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.entities.CardClassScope
import com.lvsmsmch.deckbuilder.domain.entities.CardFormatFilter
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.isPrinceRenathal
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository
import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository
import com.lvsmsmch.deckbuilder.domain.usecases.AssembleDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SaveDeckUseCase
import com.lvsmsmch.deckbuilder.domain.usecases.SearchCardsUseCase
import com.lvsmsmch.deckbuilder.presentation.ui.components.DefaultHeroes
import kotlinx.coroutines.FlowPreview
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

@OptIn(FlowPreview::class)
class DeckBuilderViewModel(
    private val searchCards: SearchCardsUseCase,
    private val assembleDeck: AssembleDeckUseCase,
    private val saveDeck: SaveDeckUseCase,
    private val rotation: RotationRepository,
    private val decks: DeckRepository,
    private val savedDecks: SavedDeckRepository,
    private val editCode: String? = null,
    private val savedName: String? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(
        if (editCode.isNullOrBlank()) BuilderState()
        else BuilderState(phase = Phase.Editing, pool = PoolState(isLoading = true)),
    )
    val state: StateFlow<BuilderState> = _state.asStateFlow()

    private val _effects = Channel<BuilderEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var poolJob: Job? = null
    private var saveJob: Job? = null
    private var pickJob: Job? = null
    private var editingOriginalCode: String? = editCode

    init {
        _state
            .map { it.pool.filters.textQuery }
            .distinctUntilChanged()
            .drop(1)
            .debounce(350L)
            .onEach { reloadPoolFirstPage() }
            .launchIn(viewModelScope)

        editCode?.takeIf { it.isNotBlank() }?.let(::loadDeckForEditing)
    }

    private fun loadDeckForEditing(code: String) {
        pickJob?.cancel()
        pickJob = viewModelScope.launch {
            when (val result = decks.decodeByCode(code)) {
                is Result.Success -> {
                    val deck = result.data
                    val classSlug = deck.heroClass?.slug ?: deck.hero?.classes?.firstOrNull()?.slug
                    val meta = deck.heroClass ?: classSlug?.let { ClassMeta(id = 0, slug = it, name = it) }
                    _state.update {
                        it.copy(
                            phase = Phase.Editing,
                            chosenClass = meta,
                            heroCardId = deck.hero?.id ?: meta?.slug?.let(DefaultHeroes::dbfIdFor),
                            format = deck.format.takeUnless { f -> f == GameFormat.UNKNOWN } ?: GameFormat.STANDARD,
                            deck = deck.cards.associateBy { entry -> entry.card.id },
                            pool = PoolState(isLoading = true),
                            saveError = null,
                        )
                    }
                    reloadPoolFirstPage()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            phase = Phase.ClassPicker,
                            saveError = result.throwable.message ?: result.throwable.javaClass.simpleName,
                        )
                    }
                    flashToast(result.throwable.message ?: "Could not load deck")
                }
            }
        }
    }

    fun pickClassBySlug(slug: String) {
        pickJob?.cancel()
        pickJob = viewModelScope.launch {
            val canonicalDbf = DefaultHeroes.dbfIdFor(slug)
            val meta = ClassMeta(id = 0, slug = slug, name = slug)
            _state.update {
                it.copy(
                    phase = Phase.Editing,
                    chosenClass = meta,
                    heroCardId = canonicalDbf,
                    deck = emptyMap(),
                    pool = PoolState(isLoading = true),
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
        _state.update { it.copy(pool = it.pool.copy(filters = it.pool.filters.copy(textQuery = query))) }
    }

    fun setPoolSort(key: SortKey, direction: SortDir) {
        val nextSort = CardSort(key = key, direction = direction)
        if (nextSort == _state.value.pool.filters.sort) return
        _state.update { it.copy(pool = it.pool.copy(filters = it.pool.filters.copy(sort = nextSort))) }
        reloadPoolFirstPage()
    }

    fun togglePoolManaCost(cost: Int) {
        _state.update {
            val current = it.pool.filters.manaCosts
            val next = if (cost in current) current - cost else current + cost
            it.copy(pool = it.pool.copy(filters = it.pool.filters.copy(manaCosts = next)))
        }
        reloadPoolFirstPage()
    }

    fun applyPoolFilters(filters: CardFilters) {
        if (filters == _state.value.pool.filters) return
        _state.update { it.copy(pool = it.pool.copy(filters = filters)) }
        reloadPoolFirstPage()
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
            return
        }
        val resultingMaxSize = if (card.isPrinceRenathal) 40 else st.maxDeckSize
        if (st.cardCount + (target - existingCount) > resultingMaxSize) {
            flashToast("Deck is full (${st.maxDeckSize}/${st.maxDeckSize})")
            return
        }
        _state.update {
            it.copy(deck = it.deck + (card.id to DeckCardEntry(card, target)))
        }
    }

    fun removeCard(card: Card) {
        if (card.isPrinceRenathal && _state.value.cardCount > 30) {
            flashToast("Remove cards down to 30 before removing Prince Renathal")
            return
        }
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
            when (val r = assembleDeck(ids = ids, heroCardId = st.heroCardId, format = st.format)) {
                is Result.Success -> {
                    val deck = r.data
                    val oldCode = editingOriginalCode
                    val name = savedName ?: oldCode?.let { savedDecks.get(it)?.name }
                    saveDeck(deck, name = name)
                    if (oldCode != null && oldCode != deck.code) {
                        runCatching { savedDecks.delete(oldCode) }
                    }
                    editingOriginalCode = deck.code
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
        val classes = when (st.pool.filters.classScope) {
            CardClassScope.ALL -> setOf(clsSlug, "neutral")
            CardClassScope.CLASS_ONLY -> setOf(clsSlug)
            CardClassScope.NEUTRAL_ONLY -> setOf("neutral")
        }
        poolJob = viewModelScope.launch {
            val filters = st.pool.filters.copy(
                classes = classes,
                collectibleOnly = true,
                format = st.format.toCardFormatFilter(),
            )
            val result = searchCards(filters, page = targetPage)

            if (result is Result.Error) {
                _state.update {
                    it.copy(
                        pool = it.pool.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = result.throwable.message,
                        ),
                    )
                }
                return@launch
            }
            result as Result.Success
            val page = result.data
            val visible = page.items.filterNot(::isDefaultHeroAvatar)

            _state.update {
                val newCards = if (replace) visible else it.pool.cards + visible
                it.copy(
                    pool = it.pool.copy(
                        cards = newCards,
                        page = page.pageNumber,
                        pageCount = page.pageCount,
                        totalCount = page.totalCount,
                        isLoading = false,
                        isLoadingMore = false,
                        contentVersion = if (replace) it.pool.contentVersion + 1 else it.pool.contentVersion,
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

    private fun isDefaultHeroAvatar(card: Card): Boolean {
        if (!card.cardType.slug.equals("hero", ignoreCase = true)) return false
        if (card.text?.isNotBlank() == true) return false
        return CanonicalHeroId.matches(card.slug)
    }

    private companion object {
        val CanonicalHeroId = Regex("""^HERO_\d+[a-z]*$""")
    }
}

private fun GameFormat.toCardFormatFilter(): CardFormatFilter = when (this) {
    GameFormat.STANDARD -> CardFormatFilter.STANDARD
    GameFormat.WILD -> CardFormatFilter.WILD
    GameFormat.CLASSIC,
    GameFormat.TWIST,
    GameFormat.UNKNOWN -> CardFormatFilter.ALL
}
