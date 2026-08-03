package com.lvsmsmch.deckbuilder.data.db

import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.CardFormatFilter
import com.lvsmsmch.deckbuilder.domain.entities.CardSort
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardQueryTest {

    private fun build(filters: CardFilters, standardSets: Set<String> = emptySet()) =
        CardQuery.build(filters, locale = "enUS", standardSets = standardSets)

    /** Every value must bind through `?`; card names carry quotes and apostrophes. */
    @Test
    fun values_are_bound_not_inlined() {
        val sql = build(CardFilters(textQuery = "Al'Akir", sets = setOf("the-barrens")))
        assertFalse(sql.where.contains("Al'Akir"))
        assertFalse(sql.where.contains("THE_BARRENS"))
        assertContains(sql.args, "%al'akir%")
        assertContains(sql.args, "THE_BARRENS")
    }

    @Test
    fun placeholder_count_matches_argument_count() {
        val sql = build(
            CardFilters(
                classes = setOf("mage", "druid"),
                sets = setOf("titans"),
                rarities = setOf("legendary"),
                types = setOf("minion"),
                minionTypes = setOf("dragon"),
                spellSchools = setOf("fire"),
                manaCosts = setOf(1, 2, 7),
                textQuery = "flame",
            ),
        )
        assertEquals(sql.where.count { it == '?' }, sql.args.size)
    }

    @Test
    fun domain_slugs_become_database_tokens() {
        val sql = build(CardFilters(classes = setOf("demonhunter"), sets = setOf("the-lost-city")))
        assertContains(sql.args, "%,DEMONHUNTER,%")
        assertContains(sql.args, "THE_LOST_CITY")
    }

    @Test
    fun collectible_only_hides_cosmetic_hero_skins() {
        val on = build(CardFilters(collectibleOnly = true))
        assertContains(on.where, "collectible = 1")
        assertContains(on.where, "type = 'HERO'")

        val off = build(CardFilters(collectibleOnly = false))
        assertFalse(off.where.contains("collectible = 1"))
    }

    @Test
    fun standard_format_restricts_to_rotation_sets() {
        val sql = build(CardFilters(format = CardFormatFilter.STANDARD), standardSets = setOf("TITANS", "CORE"))
        assertContains(sql.where, "cardSet IN")
        assertContains(sql.args, "TITANS")
    }

    /** Standard with an unknown rotation must return nothing, not everything. */
    @Test
    fun standard_format_without_rotation_data_matches_nothing() {
        val sql = build(CardFilters(format = CardFormatFilter.STANDARD), standardSets = emptySet())
        assertContains(sql.where, "0")
    }

    @Test
    fun wild_format_does_not_restrict_sets() {
        val sql = build(CardFilters(format = CardFormatFilter.WILD), standardSets = setOf("TITANS"))
        assertFalse(sql.args.contains("TITANS"))
    }

    @Test
    fun the_last_mana_chip_means_seven_and_above() {
        val sql = build(CardFilters(manaCosts = setOf(7)))
        assertContains(sql.where, "cost >= ?")
        assertContains(sql.args, 7)
    }

    @Test
    fun mana_costs_combine_exact_values_and_the_open_ended_chip() {
        val sql = build(CardFilters(manaCosts = setOf(1, 7)))
        assertContains(sql.where, "cost IN")
        assertContains(sql.where, "cost >= ?")
    }

    @Test
    fun excluded_sets_are_always_filtered_out() {
        val sql = build(CardFilters())
        assertContains(sql.args, "EXPERT1")
        assertContains(sql.where, "PLACEHOLDER%")
    }

    @Test
    fun sorting_orders_by_cost_then_name_and_flips_with_direction() {
        val asc = build(CardFilters(sort = CardSort(SortKey.MANA_COST, SortDir.ASC)))
        assertEquals("COALESCE(cost, 2147483647) ASC, name ASC", asc.orderBy)

        val desc = build(CardFilters(sort = CardSort(SortKey.MANA_COST, SortDir.DESC)))
        assertEquals("COALESCE(cost, 2147483647) DESC, name DESC", desc.orderBy)
    }

    /** Higher dbfId means newer, so "newest" ascending sorts dbfId descending. */
    @Test
    fun newest_first_sorts_by_descending_dbf_id() {
        val newest = build(CardFilters(sort = CardSort(SortKey.DATE_ADDED, SortDir.ASC)))
        assertTrue(newest.orderBy.startsWith("dbfId DESC"))

        val oldest = build(CardFilters(sort = CardSort(SortKey.DATE_ADDED, SortDir.DESC)))
        assertTrue(oldest.orderBy.startsWith("dbfId ASC"))
    }

    @Test
    fun grouping_by_class_sorts_class_then_cost_then_name() {
        val sql = build(CardFilters(sort = CardSort(SortKey.GROUP_BY_CLASS, SortDir.ASC)))
        assertEquals("COALESCE(cardClass, '') ASC, COALESCE(cost, 2147483647) ASC, name ASC", sql.orderBy)
    }

    @Test
    fun text_search_is_lowercased_to_match_the_precomputed_column() {
        val sql = build(CardFilters(textQuery = "  FIREBALL  "))
        assertContains(sql.where, "searchText LIKE ?")
        assertContains(sql.args, "%fireball%")
    }

    @Test
    fun locale_is_always_the_first_binding() {
        val sql = build(CardFilters())
        assertEquals("enUS", sql.args.first())
        assertTrue(sql.where.startsWith("locale = ?"))
    }
}
