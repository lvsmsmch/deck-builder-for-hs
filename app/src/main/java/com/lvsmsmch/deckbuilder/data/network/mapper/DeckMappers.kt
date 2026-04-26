package com.lvsmsmch.deckbuilder.data.network.mapper

import com.lvsmsmch.deckbuilder.data.network.dto.DeckDto
import com.lvsmsmch.deckbuilder.domain.entities.ClassMeta
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.DeckSideboard
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.Metadata

fun DeckDto.toDomain(metadata: Metadata): Deck {
    val groupedCards = cards.groupBy { it.id }
    val entries = groupedCards.values.map { dupes ->
        DeckCardEntry(card = dupes.first().toDomain(metadata), count = dupes.size)
    }.sortedWith(compareBy({ it.card.manaCost }, { it.card.name }))

    val sideboards = sideboardCards.map { sb ->
        val grouped = sb.cardsInSideboard.groupBy { it.id }.values.map { dupes ->
            DeckCardEntry(card = dupes.first().toDomain(metadata), count = dupes.size)
        }.sortedWith(compareBy({ it.card.manaCost }, { it.card.name }))
        DeckSideboard(
            owner = sb.sideboardCard.toDomain(metadata),
            cards = grouped,
        )
    }

    val resolvedClass: ClassMeta? = heroClass?.let { hc ->
        metadata.classes[hc.id] ?: ClassMeta(id = hc.id, slug = hc.slug, name = hc.name)
    }

    val resolvedHero = (hero ?: heroes.firstOrNull())?.toDomain(metadata)

    return Deck(
        code = deckCode.orEmpty(),
        format = GameFormat.fromApi(format),
        hero = resolvedHero,
        heroClass = resolvedClass,
        cards = entries,
        sideboardCards = sideboards,
        invalidCardIds = invalidCardIds,
    )
}
