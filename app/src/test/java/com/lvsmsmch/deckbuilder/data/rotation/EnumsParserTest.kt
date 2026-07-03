package com.lvsmsmch.deckbuilder.data.rotation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

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
    fun `parses current STANDARD_SETS mapping by rotation date`() {
        val sample = """
            STANDARD_SETS = {
                ZodiacYear.PEGASUS: [
                    CardSet.CORE,
                    CardSet.WILD_WEST,
                ],
                ZodiacYear.RAPTOR: [
                    CardSet.CORE, CardSet.EVENT,
                    CardSet.WHIZBANGS_WORKSHOP, CardSet.ISLAND_VACATION,
                    CardSet.SPACE, CardSet.EMERALD_DREAM,
                ],
                ZodiacYear.SCARAB: [
                    CardSet.CORE, CardSet.EVENT,
                    CardSet.EMERALD_DREAM, CardSet.THE_LOST_CITY,
                ],
            }

            ZODIAC_ROTATION_DATES = {
                ZodiacYear.PEGASUS: datetime(2024, 3, 19),
                ZodiacYear.RAPTOR: datetime(2025, 3, 25),
                ZodiacYear.SCARAB: datetime(2026, 3, 17),
            }
        """.trimIndent()

        val parsed = EnumsParser.parseStandardSets(sample, LocalDate.of(2025, 7, 1))
        assertEquals(
            setOf("CORE", "EVENT", "WHIZBANGS_WORKSHOP", "ISLAND_VACATION", "SPACE", "EMERALD_DREAM"),
            parsed,
        )
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
