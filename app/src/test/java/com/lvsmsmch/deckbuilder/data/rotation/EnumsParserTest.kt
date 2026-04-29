package com.lvsmsmch.deckbuilder.data.rotation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumsParserTest {

    private val sample = """
        from enum import IntEnum

        class CardSet(IntEnum):
            INVALID = 0
            CORE = 1637
            EXPERT1 = 3   # legacy
            BLACK_TEMPLE = 1463
            PLACEHOLDER_202204 = 1700

        class Race(IntEnum):
            DEMON = 15

        STANDARD_SETS = (
            CardSet.CORE,
            CardSet.BLACK_TEMPLE,  # current expansion
            CardSet.PLACEHOLDER_202204,
        )
    """.trimIndent()

    @Test
    fun `parses STANDARD_SETS tuple with comments`() {
        val parsed = EnumsParser.parseStandardSets(sample)
        assertEquals(setOf("CORE", "BLACK_TEMPLE", "PLACEHOLDER_202204"), parsed)
    }

    @Test
    fun `parses CardSet enum members and stops at next class`() {
        val parsed = EnumsParser.parseCardSetEnum(sample)
        assertEquals(setOf("INVALID", "CORE", "EXPERT1", "BLACK_TEMPLE", "PLACEHOLDER_202204"), parsed)
        assertTrue("DEMON" !in parsed)
    }

    @Test
    fun `returns empty when STANDARD_SETS missing`() {
        assertEquals(emptySet<String>(), EnumsParser.parseStandardSets("class Foo: pass"))
    }
}
