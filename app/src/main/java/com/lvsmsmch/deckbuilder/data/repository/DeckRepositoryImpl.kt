package com.lvsmsmch.deckbuilder.data.repository

import android.util.Log
import com.lvsmsmch.deckbuilder.data.deckstring.Deckstring
import com.lvsmsmch.deckbuilder.data.deckstring.DeckstringCard
import com.lvsmsmch.deckbuilder.data.deckstring.DeckstringFormat
import com.lvsmsmch.deckbuilder.data.deckstring.DeckstringPayload
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.runCatchingResult
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.DeckSideboard
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.repositories.CardRepository
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DB.DeckRepo"

class DeckRepositoryImpl(
    private val cards: CardRepository,
    private val locales: CurrentLocaleProvider,
) : DeckRepository {

    override suspend fun decodeByCode(code: String, locale: String?): Result<Deck> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val resolved = locales.resolve(locale)
                val payload = Deckstring.decode(code)
                buildDeck(code = code, payload = payload, locale = resolved)
            }.also { r ->
                val codePreview = code.take(12) + if (code.length > 12) "…" else ""
                when (r) {
                    is Result.Success -> Log.i(
                        TAG,
                        "decodeByCode: OK code=$codePreview cards=${r.data.cardCount} hero=${r.data.heroClass?.slug}",
                    )
                    is Result.Error -> Log.w(
                        TAG, "decodeByCode: FAILED code=$codePreview: ${r.throwable.message}", r.throwable,
                    )
                }
            }
        }

    override suspend fun assembleByIds(
        ids: List<Int>,
        heroCardId: Int?,
        locale: String?,
    ): Result<Deck> = withContext(Dispatchers.IO) {
        runCatchingResult {
            require(ids.isNotEmpty()) { "Deck must have at least one card" }
            require(heroCardId != null) { "heroCardId is required to assemble a deck" }
            val resolved = locales.resolve(locale)
            val grouped = ids.groupingBy { it }.eachCount()
            val cardEntries = grouped.entries.map { (dbf, count) -> DeckstringCard(dbf, count) }
            val payload = DeckstringPayload(
                format = DeckstringFormat.WILD,
                heroes = listOf(heroCardId),
                cards = cardEntries,
            )
            val code = Deckstring.encode(payload)
            buildDeck(code = code, payload = payload, locale = resolved)
        }.also { r ->
            when (r) {
                is Result.Success -> Log.i(
                    TAG,
                    "assembleByIds: OK ids=${ids.size} hero=$heroCardId → code='${r.data.code.take(12)}…' cards=${r.data.cardCount}",
                )
                is Result.Error -> Log.w(
                    TAG, "assembleByIds: FAILED ids=${ids.size} hero=$heroCardId: ${r.throwable.message}", r.throwable,
                )
            }
        }
    }

    private suspend fun buildDeck(code: String, payload: DeckstringPayload, locale: String): Deck {
        val resolved = mutableMapOf<Int, Card>()
        val invalid = mutableListOf<Int>()
        val allDbfIds = buildSet {
            addAll(payload.heroes)
            payload.cards.forEach { add(it.dbfId) }
            payload.sideboards.forEach { add(it.dbfId); add(it.ownerDbfId) }
        }
        for (dbf in allDbfIds) {
            when (val r = cards.getCard(dbf.toString(), locale)) {
                is Result.Success -> resolved[dbf] = r.data
                is Result.Error -> invalid += dbf
            }
        }

        val hero = payload.heroes.firstOrNull()?.let { resolved[it] }
        val heroClass = hero?.classes?.firstOrNull { !it.slug.equals("neutral", true) }
            ?: deriveClassFromCards(payload.cards, resolved)

        val entries = payload.cards
            .mapNotNull { dc -> resolved[dc.dbfId]?.let { DeckCardEntry(it, dc.count) } }
            .sortedWith(compareBy({ it.card.manaCost }, { it.card.name }))

        val sideboards = payload.sideboards
            .groupBy { it.ownerDbfId }
            .mapNotNull { (ownerId, sbs) ->
                val owner = resolved[ownerId] ?: return@mapNotNull null
                val sbCards = sbs
                    .mapNotNull { sb -> resolved[sb.dbfId]?.let { DeckCardEntry(it, sb.count) } }
                    .sortedWith(compareBy({ it.card.manaCost }, { it.card.name }))
                DeckSideboard(owner = owner, cards = sbCards)
            }

        return Deck(
            code = code,
            format = payload.format.toGameFormat(),
            hero = hero,
            heroClass = heroClass,
            cards = entries,
            sideboardCards = sideboards,
            invalidCardIds = invalid,
        )
    }

    private fun deriveClassFromCards(
        cards: List<DeckstringCard>,
        resolved: Map<Int, Card>,
    ): ClassMeta? = cards
        .asSequence()
        .mapNotNull { resolved[it.dbfId] }
        .flatMap { it.classes.asSequence() }
        .firstOrNull { !it.slug.equals("neutral", true) }
}

private fun DeckstringFormat.toGameFormat(): GameFormat = when (this) {
    DeckstringFormat.WILD -> GameFormat.WILD
    DeckstringFormat.STANDARD -> GameFormat.STANDARD
    DeckstringFormat.CLASSIC -> GameFormat.CLASSIC
    DeckstringFormat.TWIST -> GameFormat.TWIST
}
