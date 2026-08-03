package com.lvsmsmch.deckbuilder.data.db

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HiddenCardSetsTest {

    @Test
    fun classic_duplicate_buckets_are_hidden() {
        assertTrue(HiddenCardSets.isHidden("EXPERT1"))
        assertTrue(HiddenCardSets.isHidden("VANILLA"))
        assertTrue(HiddenCardSets.isHidden("LEGACY"))
    }

    /**
     * The set that used to raise a permanent "rotation out of date" warning:
     * an internal bucket of Core cards retired in April 2022.
     */
    @Test
    fun internal_placeholder_buckets_are_hidden() {
        assertTrue(HiddenCardSets.isHidden("PLACEHOLDER_202204"))
        assertTrue(HiddenCardSets.isHidden("PLACEHOLDER_999999"))
    }

    @Test
    fun real_expansions_stay_visible() {
        assertFalse(HiddenCardSets.isHidden("CORE"))
        assertFalse(HiddenCardSets.isHidden("TITANS"))
        assertFalse(HiddenCardSets.isHidden("THE_LOST_CITY"))
        assertFalse(HiddenCardSets.isHidden("EVENT"))
    }

    @Test
    fun a_missing_set_is_not_hidden() {
        assertFalse(HiddenCardSets.isHidden(null))
    }
}
