package com.lvsmsmch.deckbuilder.data.hsjson

import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity
import com.lvsmsmch.deckbuilder.data.hsjson.dto.HsJsonCardDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

internal fun HsJsonCardDto.toEntity(locale: String, json: Json): HsJsonCardEntity {
    val races = races ?: race?.let { listOf(it) } ?: emptyList()
    return HsJsonCardEntity(
        locale = locale,
        dbfId = dbfId,
        cardId = id,
        name = name.orEmpty(),
        text = text,
        cost = cost,
        attack = attack,
        health = health,
        durability = durability,
        armor = armor,
        cardClass = cardClass,
        classesCsv = classes?.takeIf { it.isNotEmpty() }?.joinToCsv(),
        multiClassGroup = multiClassGroup,
        cardSet = cardSet,
        type = type,
        rarity = rarity,
        raceCsv = races.takeIf { it.isNotEmpty() }?.joinToCsv(),
        spellSchool = spellSchool,
        mechanicsCsv = mechanics?.takeIf { it.isNotEmpty() }?.joinToCsv(),
        collectible = collectible == true,
        payloadJson = json.encodeToString(this),
    )
}

private fun List<String>.joinToCsv(): String =
    joinToString(separator = ",", prefix = ",", postfix = ",")
