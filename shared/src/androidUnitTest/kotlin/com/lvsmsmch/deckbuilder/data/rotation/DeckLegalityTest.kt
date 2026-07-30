package com.lvsmsmch.deckbuilder.data.rotation

import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardType
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.DeckCardEntry
import com.lvsmsmch.deckbuilder.domain.entities.Expansion
import com.lvsmsmch.deckbuilder.domain.entities.GameFormat
import com.lvsmsmch.deckbuilder.domain.entities.RotationStatus
import com.lvsmsmch.deckbuilder.domain.entities.StandardRotation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckLegalityTest {

    private val rotation = StandardRotation(
        standardSets = setOf("CORE", "BLACK_TEMPLE", "TITANS"),
        knownSets = setOf("CORE", "BLACK_TEMPLE", "TITANS", "EXPERT1", "ULDUM"),
        sourceSha = "abc123",
        sourceCommittedAtIso = "2026-04-01T00:00:00Z",
        fetchedAtMs = 0L,
    )

    private fun card(id: Int, setSlug: String?): Card = Card(
        id = id,
        slug = "C$id",
        name = "Card $id",
        text = null,
        flavorText = null,
        image = "",
        cropImage = null,
        artistName = null,
        manaCost = 1,
        attack = null,
        health = null,
        durability = null,
        armor = null,
        classes = emptyList(),
        cardSet = setSlug?.let { Expansion(0, it, it, null) },
        rarity = null,
        cardType = CardType(0, "minion", "Minion"),
        minionType = null,
        spellSchool = null,
        keywords = emptyList(),
        collectible = true,
        childIds = emptyList(),
    )

    private fun deck(vararg entries: Pair<Card, Int>) = Deck(
        code = "AAEB",
        format = GameFormat.STANDARD,
        hero = null,
        heroClass = null,
        cards = entries.map { (c, n) -> DeckCardEntry(c, n) },
    )

    @Test
    fun `card from standard set is legal`() {
        // domain slug "black-temple" must be re-uppercased to BLACK_TEMPLE
        assertTrue(isStandardLegal(card(1, "black-temple"), rotation))
        assertTrue(isStandardLegal(card(2, "core"), rotation))
    }

    @Test
    fun `card from rotated-out set is illegal`() {
        assertFalse(isStandardLegal(card(1, "expert1"), rotation))
        assertFalse(isStandardLegal(card(2, "uldum"), rotation))
    }

    @Test
    fun `card without a set is illegal`() {
        assertFalse(isStandardLegal(card(1, null), rotation))
    }

    @Test
    fun `deck is legal only when every card is standard`() {
        val good = deck(card(1, "core") to 2, card(2, "titans") to 1)
        assertTrue(isStandardLegal(good, rotation))

        val bad = deck(card(1, "core") to 2, card(2, "expert1") to 1)
        assertFalse(isStandardLegal(bad, rotation))
    }

    @Test
    fun `rotated out lists only out-of-standard entries`() {
        val mixed = deck(
            card(1, "core") to 2,
            card(2, "expert1") to 1,
            card(3, "uldum") to 2,
        )
        val out = rotatedOut(mixed, rotation)
        assertEquals(listOf(2, 3), out.map { it.card.id })
    }

    @Test
    fun `rotation status flags unknown collectible sets as outdated`() {
        val collectible = setOf("CORE", "BLACK_TEMPLE", "TITANS", "FRESHLY_ADDED")
        val unknown = collectible - rotation.knownSets
        val status = RotationStatus(rotation, unknown)
        assertEquals(setOf("FRESHLY_ADDED"), status.unknownSets)
        assertTrue(status.isOutdated)
    }

    @Test
    fun `rotation status not outdated when all sets known`() {
        val collectible = setOf("CORE", "BLACK_TEMPLE", "EXPERT1")
        val unknown = collectible - rotation.knownSets
        val status = RotationStatus(rotation, unknown)
        assertTrue(status.unknownSets.isEmpty())
        assertFalse(status.isOutdated)
    }

    @Test
    fun `domain slug is canonicalised to rotation token`() {
        assertEquals("DEATH_KNIGHT", "death-knight".toRotationToken())
        assertEquals("BLACK_TEMPLE", "black-temple".toRotationToken())
        assertEquals("CORE", "core".toRotationToken())
    }
}
