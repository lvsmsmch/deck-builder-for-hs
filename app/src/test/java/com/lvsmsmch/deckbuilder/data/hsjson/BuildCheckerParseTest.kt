package com.lvsmsmch.deckbuilder.data.hsjson

import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BuildCheckerParseTest {

    private val checker = BuildChecker(OkHttpClient())

    @Test
    fun `parses build segment from canonical redirect target`() {
        val url = "https://api.hearthstonejson.com/v1/231075/enUS/cards.collectible.json"
        assertEquals("231075", checker.parseBuildFromUrl(url))
    }

    @Test
    fun `parses build with non-default locale`() {
        val url = "https://api.hearthstonejson.com/v1/200000/ruRU/cards.collectible.json"
        assertEquals("200000", checker.parseBuildFromUrl(url))
    }

    @Test
    fun `returns null for non-numeric build segment`() {
        val url = "https://api.hearthstonejson.com/v1/latest/enUS/cards.collectible.json"
        assertNull(checker.parseBuildFromUrl(url))
    }

    @Test
    fun `returns null when v1 segment missing`() {
        val url = "https://api.hearthstonejson.com/cards/231075/enUS/cards.collectible.json"
        assertNull(checker.parseBuildFromUrl(url))
    }

    @Test
    fun `returns null for empty url`() {
        assertNull(checker.parseBuildFromUrl(""))
    }
}
