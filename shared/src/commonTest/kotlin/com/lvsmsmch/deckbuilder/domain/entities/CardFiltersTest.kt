package com.lvsmsmch.deckbuilder.domain.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardFiltersTest {

    @Test
    fun empty_filters_are_inactive() {
        val filters = CardFilters()
        assertFalse(filters.hasFilters)
        assertEquals(0, filters.activeFilterCount)
    }

    @Test
    fun each_group_counts_once_regardless_of_how_many_values_it_holds() {
        val filters = CardFilters(
            classes = setOf("mage", "druid", "rogue"),
            manaCosts = setOf(1, 2, 3),
        )
        assertEquals(2, filters.activeFilterCount)
        assertTrue(filters.hasFilters)
    }

    @Test
    fun showing_non_collectible_cards_counts_as_a_filter() {
        assertEquals(1, CardFilters(collectibleOnly = false).activeFilterCount)
        assertEquals(0, CardFilters(collectibleOnly = true).activeFilterCount)
    }

    @Test
    fun text_query_counts_only_when_not_blank() {
        assertEquals(0, CardFilters(textQuery = "   ").activeFilterCount)
        assertEquals(1, CardFilters(textQuery = "fireball").activeFilterCount)
    }

    @Test
    fun sorting_is_not_a_filter() {
        val sorted = CardFilters(sort = CardSort(SortKey.NAME, SortDir.DESC))
        assertEquals(0, sorted.activeFilterCount)
        assertFalse(sorted.hasFilters)
    }

    @Test
    fun class_scope_and_format_count() {
        assertEquals(1, CardFilters(classScope = CardClassScope.NEUTRAL_ONLY).activeFilterCount)
        assertEquals(1, CardFilters(format = CardFormatFilter.STANDARD).activeFilterCount)
    }
}
