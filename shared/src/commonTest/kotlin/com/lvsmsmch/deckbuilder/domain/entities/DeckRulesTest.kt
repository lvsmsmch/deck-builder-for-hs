package com.lvsmsmch.deckbuilder.domain.entities

import com.lvsmsmch.deckbuilder.testCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeckRulesTest {

    private val renathal = testCard(id = 79767, slug = "REV_018", name = "Prince Renathal")
    private val coreRenathal = testCard(id = 111689, slug = "CORE_REV_018", name = "Prince Renathal")
    private val whizbang = testCard(id = 50477, slug = "BOT_914", name = "Whizbang the Wonderful")
    private val plain = testCard(id = 1)

    @Test
    fun default_deck_holds_thirty_cards() {
        assertEquals(30, DeckRules.maxCardCountFor(listOf(plain)))
        assertEquals(30, DeckRules.maxCardCountForDbfIds(listOf(1, 2, 3)))
    }

    @Test
    fun renathal_raises_the_limit_to_forty() {
        assertEquals(40, DeckRules.maxCardCountFor(listOf(plain, renathal)))
        assertEquals(40, DeckRules.maxCardCountFor(listOf(plain, coreRenathal)))
        assertEquals(40, DeckRules.maxCardCountForDbfIds(listOf(1, 79767)))
    }

    @Test
    fun whizbang_wins_over_renathal() {
        assertEquals(1, DeckRules.maxCardCountFor(listOf(renathal, whizbang)))
        assertEquals(1, DeckRules.maxCardCountForDbfIds(listOf(79767, 50477)))
    }

    /** The saved-deck list only has dbfIds; it must agree with the deck screen. */
    @Test
    fun card_based_and_dbf_based_limits_agree() {
        val cards = listOf(plain, renathal)
        assertEquals(
            DeckRules.maxCardCountFor(cards),
            DeckRules.maxCardCountForDbfIds(cards.map { it.id }),
        )
    }

    @Test
    fun special_cards_are_recognised_by_id_and_by_slug() {
        assertTrue(renathal.isPrinceRenathal)
        assertTrue(coreRenathal.isPrinceRenathal)
        assertTrue(whizbang.isWhizbangDeck)
        assertFalse(plain.isPrinceRenathal)
        assertFalse(plain.isWhizbangDeck)
    }

    @Test
    fun default_hero_avatars_are_detected_by_canonical_id() {
        assertTrue(testCard(id = 7, slug = "HERO_01", text = null, typeSlug = "hero").isDefaultHeroAvatar)
        assertTrue(testCard(id = 8, slug = "HERO_06a", text = "", typeSlug = "hero").isDefaultHeroAvatar)
        // Alternate skins and real hero cards must stay visible.
        assertFalse(testCard(id = 9, slug = "HERO_06p", text = "Wildheart Guff", typeSlug = "hero").isDefaultHeroAvatar)
        assertFalse(testCard(id = 10, slug = "ICC_481", text = "Battlecry:", typeSlug = "hero").isDefaultHeroAvatar)
        assertFalse(testCard(id = 11, slug = "HERO_01", text = null, typeSlug = "minion").isDefaultHeroAvatar)
    }
}
