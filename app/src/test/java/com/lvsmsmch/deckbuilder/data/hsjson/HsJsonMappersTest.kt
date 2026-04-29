package com.lvsmsmch.deckbuilder.data.hsjson

import com.lvsmsmch.deckbuilder.data.hsjson.dto.HsJsonCardDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HsJsonMappersTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    @Test
    fun `dto to entity copies hot fields and serialises payload`() {
        val dto = HsJsonCardDto(
            id = "EX1_001",
            dbfId = 559,
            name = "Leeroy Jenkins",
            text = "Charge",
            cost = 5,
            attack = 6,
            health = 2,
            cardClass = "NEUTRAL",
            cardSet = "EXPERT1",
            type = "MINION",
            rarity = "LEGENDARY",
            race = "DEMON",
            mechanics = listOf("CHARGE", "BATTLECRY"),
            collectible = true,
        )
        val entity = dto.toEntity(locale = "enUS", json = json)

        assertEquals("enUS", entity.locale)
        assertEquals(559, entity.dbfId)
        assertEquals("EX1_001", entity.cardId)
        assertEquals("Leeroy Jenkins", entity.name)
        assertEquals("NEUTRAL", entity.cardClass)
        assertEquals("EXPERT1", entity.cardSet)
        assertEquals("LEGENDARY", entity.rarity)
        assertEquals(",DEMON,", entity.raceCsv)
        assertEquals(",CHARGE,BATTLECRY,", entity.mechanicsCsv)
        assertTrue(entity.collectible)
        assertTrue(entity.payloadJson.contains("\"id\":\"EX1_001\""))
    }

    @Test
    fun `dto with empty races and mechanics yields null csv`() {
        val dto = HsJsonCardDto(id = "X", dbfId = 1, mechanics = emptyList(), races = emptyList())
        val entity = dto.toEntity(locale = "enUS", json = json)
        assertNull(entity.raceCsv)
        assertNull(entity.mechanicsCsv)
    }

    @Test
    fun `dto with multi-class list takes precedence over single cardClass`() {
        val dto = HsJsonCardDto(
            id = "X",
            dbfId = 1,
            cardClass = "MAGE",
            classes = listOf("MAGE", "PRIEST"),
        )
        val entity = dto.toEntity(locale = "enUS", json = json)
        assertEquals(",MAGE,PRIEST,", entity.classesCsv)
        assertEquals("MAGE", entity.cardClass)
    }

    @Test
    fun `entity to domain projects tokens to slug form and builds image urls`() {
        val dto = HsJsonCardDto(
            id = "BT_001",
            dbfId = 12345,
            name = "Sample",
            text = "Battlecry: do something.",
            cost = 3,
            attack = 4,
            health = 5,
            cardClass = "DEMONHUNTER",
            cardSet = "BLACK_TEMPLE",
            type = "MINION",
            rarity = "RARE",
            race = "DEMON",
            spellSchool = "FEL",
            mechanics = listOf("BATTLECRY", "DEATHRATTLE"),
            collectible = true,
        )
        val entity = dto.toEntity(locale = "enUS", json = json)
        val card = entity.toDomain()

        assertEquals(12345, card.id)
        assertEquals("BT_001", card.slug)
        assertEquals("Sample", card.name)
        assertEquals(3, card.manaCost)
        assertEquals("demonhunter", card.classes.single().slug)
        assertEquals("black-temple", card.cardSet?.slug)
        assertEquals("rare", card.rarity?.slug)
        assertEquals("minion", card.cardType.slug)
        assertEquals("demon", card.minionType?.slug)
        assertEquals("fel", card.spellSchool?.slug)
        assertEquals(listOf("battlecry", "deathrattle"), card.keywords.map { it.slug })
        assertEquals(
            "https://art.hearthstonejson.com/v1/render/latest/enUS/512x/BT_001.png",
            card.image,
        )
        assertEquals("https://art.hearthstonejson.com/v1/tiles/BT_001.png", card.cropImage)
        assertTrue(card.collectible)
    }

    @Test
    fun `entity to domain falls back to unknown card type`() {
        val dto = HsJsonCardDto(id = "X", dbfId = 1, name = "X")
        val entity = dto.toEntity(locale = "enUS", json = json)
        val card = entity.toDomain()
        assertEquals("unknown", card.cardType.slug)
        assertNull(card.cardSet)
        assertNull(card.minionType)
        assertFalse(card.collectible)
    }

    @Test
    fun `class tokens prefer multi-class list`() {
        val dto = HsJsonCardDto(
            id = "X", dbfId = 1, cardClass = "MAGE", classes = listOf("MAGE", "PRIEST"),
        )
        val entity = dto.toEntity(locale = "enUS", json = json)
        assertEquals(listOf("MAGE", "PRIEST"), entity.parseClassTokens())
    }

    @Test
    fun `class tokens fall back to cardClass when multi list absent`() {
        val dto = HsJsonCardDto(id = "X", dbfId = 1, cardClass = "WARRIOR")
        val entity = dto.toEntity(locale = "enUS", json = json)
        assertEquals(listOf("WARRIOR"), entity.parseClassTokens())
    }

    @Test
    fun `domain slug normalises underscores to dashes and lowercases`() {
        assertEquals("death-knight", "DEATH_KNIGHT".toDomainSlug())
        assertEquals("demonhunter", "DEMONHUNTER".toDomainSlug())
        assertEquals("expert1", "EXPERT1".toDomainSlug())
    }

    @Test
    fun `dto with collectible null becomes false on entity`() {
        val dto = HsJsonCardDto(id = "X", dbfId = 1, collectible = null)
        val entity = dto.toEntity(locale = "enUS", json = json)
        assertFalse(entity.collectible)
        assertNotNull(entity.payloadJson)
    }
}
