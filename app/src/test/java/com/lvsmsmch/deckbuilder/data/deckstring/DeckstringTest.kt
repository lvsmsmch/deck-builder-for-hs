package com.lvsmsmch.deckbuilder.data.deckstring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class DeckstringTest {

    private val sampleHero = 7 // Garrosh / dbfId 7
    private val sampleCards = listOf(
        DeckstringCard(dbfId = 559, count = 1),  // Leeroy
        DeckstringCard(dbfId = 1000, count = 2),
        DeckstringCard(dbfId = 1234, count = 2),
        DeckstringCard(dbfId = 200, count = 3),  // x3 (xN bucket)
    )

    @Test
    fun `roundtrip preserves payload`() {
        val payload = DeckstringPayload(
            format = DeckstringFormat.STANDARD,
            heroes = listOf(sampleHero),
            cards = sampleCards,
        )
        val code = Deckstring.encode(payload)
        val decoded = Deckstring.decode(code)

        assertEquals(payload.format, decoded.format)
        assertEquals(payload.heroes, decoded.heroes)
        assertEquals(payload.cards.toSet(), decoded.cards.toSet())
        assertTrue(decoded.sideboards.isEmpty())
    }

    @Test
    fun `roundtrip with sideboard`() {
        val payload = DeckstringPayload(
            format = DeckstringFormat.WILD,
            heroes = listOf(sampleHero),
            cards = sampleCards,
            sideboards = listOf(
                DeckstringSideboardCard(dbfId = 90001, count = 1, ownerDbfId = 559),
                DeckstringSideboardCard(dbfId = 90002, count = 2, ownerDbfId = 559),
                DeckstringSideboardCard(dbfId = 90003, count = 3, ownerDbfId = 559),
            ),
        )
        val decoded = Deckstring.decode(Deckstring.encode(payload))
        assertEquals(payload.sideboards.toSet(), decoded.sideboards.toSet())
    }

    @Test
    fun `encode is deterministic regardless of input order`() {
        val a = DeckstringPayload(
            format = DeckstringFormat.STANDARD,
            heroes = listOf(sampleHero),
            cards = listOf(
                DeckstringCard(559, 1),
                DeckstringCard(1000, 2),
                DeckstringCard(1234, 2),
                DeckstringCard(200, 3),
            ),
        )
        val b = a.copy(cards = a.cards.reversed())
        assertEquals(Deckstring.encode(a), Deckstring.encode(b))
    }

    @Test
    fun `header bytes are reserved-zero and version-one`() {
        val payload = DeckstringPayload(
            format = DeckstringFormat.STANDARD,
            heroes = listOf(sampleHero),
            cards = listOf(DeckstringCard(559, 1)),
        )
        val raw = Base64.getDecoder().decode(Deckstring.encode(payload))
        assertEquals(0, raw[0].toInt() and 0xFF)  // reserved byte
        assertEquals(1, raw[1].toInt() and 0xFF)  // version varint (single byte)
        assertEquals(2, raw[2].toInt() and 0xFF)  // format varint = STANDARD (2)
    }

    @Test
    fun `decode rejects empty string`() {
        assertThrows(IllegalArgumentException::class.java) {
            Deckstring.decode("")
        }
    }

    @Test
    fun `decode rejects garbage base64`() {
        assertThrows(IllegalArgumentException::class.java) {
            Deckstring.decode("!!!not-base64!!!")
        }
    }

    @Test
    fun `decode rejects bad reserved byte`() {
        // craft a payload with first byte != 0
        val bad = byteArrayOf(0x01, 0x01, 0x02, 0x01, 0x07, 0x00, 0x00, 0x00)
        val str = Base64.getEncoder().encodeToString(bad)
        assertThrows(IllegalArgumentException::class.java) { Deckstring.decode(str) }
    }

    @Test
    fun `decode rejects unknown version`() {
        // 0x00 reserved, 0x09 version=9
        val bad = byteArrayOf(0x00, 0x09, 0x02, 0x01, 0x07, 0x00, 0x00, 0x00)
        val str = Base64.getEncoder().encodeToString(bad)
        assertThrows(IllegalArgumentException::class.java) { Deckstring.decode(str) }
    }

    @Test
    fun `decode rejects unknown format code`() {
        val bad = byteArrayOf(0x00, 0x01, 0x09, 0x01, 0x07, 0x00, 0x00, 0x00)
        val str = Base64.getEncoder().encodeToString(bad)
        assertThrows(IllegalArgumentException::class.java) { Deckstring.decode(str) }
    }

    @Test
    fun `encode rejects payload without heroes`() {
        val payload = DeckstringPayload(
            format = DeckstringFormat.STANDARD,
            heroes = emptyList(),
            cards = listOf(DeckstringCard(559, 1)),
        )
        assertThrows(IllegalArgumentException::class.java) { Deckstring.encode(payload) }
    }

    @Test
    fun `encode rejects non-positive counts`() {
        val payload = DeckstringPayload(
            format = DeckstringFormat.STANDARD,
            heroes = listOf(sampleHero),
            cards = listOf(DeckstringCard(559, 0)),
        )
        assertThrows(IllegalArgumentException::class.java) { Deckstring.encode(payload) }
    }

    @Test
    fun `varint handles multi-byte dbfIds`() {
        // dbfIds well above 127 force multi-byte varint encoding
        val payload = DeckstringPayload(
            format = DeckstringFormat.WILD,
            heroes = listOf(31),
            cards = listOf(
                DeckstringCard(dbfId = 99999, count = 1),
                DeckstringCard(dbfId = 200000, count = 2),
                DeckstringCard(dbfId = 300000, count = 4),
            ),
        )
        val decoded = Deckstring.decode(Deckstring.encode(payload))
        assertEquals(payload.cards.toSet(), decoded.cards.toSet())
    }

    @Test
    fun `30-card deck typical for constructed roundtrips`() {
        // 15 unique dbfIds × 2 = 30 cards (typical Standard deck shape)
        val cards = (1000..1014).map { DeckstringCard(dbfId = it, count = 2) }
        val payload = DeckstringPayload(
            format = DeckstringFormat.STANDARD,
            heroes = listOf(31),
            cards = cards,
        )
        val decoded = Deckstring.decode(Deckstring.encode(payload))
        assertEquals(cards.toSet(), decoded.cards.toSet())
        assertEquals(30, decoded.cards.sumOf { it.count })
    }
}
