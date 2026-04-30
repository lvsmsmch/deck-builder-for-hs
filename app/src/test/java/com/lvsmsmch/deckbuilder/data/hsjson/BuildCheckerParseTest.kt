package com.lvsmsmch.deckbuilder.data.hsjson

import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BuildCheckerParseTest {

    private val checker = BuildChecker(OkHttpClient())

    @Test
    fun `parses first build link from tree-style index`() {
        val html = """
            <html><body>
            <h1>HearthstoneJSON</h1><p>
            <a href="/v1/241135">/v1/241135</a><br>
            ├── <a href="/v1/241135/all/">all</a><br>
            │   ├── <a href="/v1/241135/all/cards.collectible.json">cards.collectible.json</a><br>
            </body></html>
        """.trimIndent()
        assertEquals("241135", checker.parseBuildFromIndex(html))
    }

    @Test
    fun `picks the first numeric build even when older ones are listed`() {
        // The /v1/ root index lists every build; latest must come first.
        val html = """
            <a href="/v1/241135">latest</a>
            <a href="/v1/240000">prev</a>
            <a href="/v1/190920">old</a>
        """.trimIndent()
        assertEquals("241135", checker.parseBuildFromIndex(html))
    }

    @Test
    fun `tolerates trailing slash on build link`() {
        val html = """<a href="/v1/241135/">latest</a>"""
        assertEquals("241135", checker.parseBuildFromIndex(html))
    }

    @Test
    fun `returns null when no build link present`() {
        val html = "<html><body>nothing here</body></html>"
        assertNull(checker.parseBuildFromIndex(html))
    }

    @Test
    fun `ignores non-numeric segment after v1`() {
        val html = """<a href="/v1/latest/">latest</a>"""
        assertNull(checker.parseBuildFromIndex(html))
    }

    @Test
    fun `returns null for empty html`() {
        assertNull(checker.parseBuildFromIndex(""))
    }
}
