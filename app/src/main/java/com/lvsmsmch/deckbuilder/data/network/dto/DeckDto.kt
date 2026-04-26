package com.lvsmsmch.deckbuilder.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response shape of `GET /hearthstone/deck?code=...` and the
 * `?ids=...&hero=...` assemble flavor. The API returns one entry per card
 * copy in `cards` (so a 2× card appears twice). We group + count in the
 * mapper.
 *
 * `class` is reserved in Kotlin so we serialize from the JSON `class` field.
 */
@Serializable
data class DeckDto(
    @SerialName("deckCode") val deckCode: String? = null,
    val format: String? = null,
    val hero: CardDto? = null,
    val heroes: List<CardDto> = emptyList(),
    @SerialName("class") val heroClass: ClassDto? = null,
    @SerialName("heroPower") val heroPower: CardDto? = null,
    val cards: List<CardDto> = emptyList(),
    val sideboardCards: List<SideboardDto> = emptyList(),
    val cardCount: Int? = null,
    val invalidCardIds: List<Int> = emptyList(),
)

@Serializable
data class SideboardDto(
    val sideboardCard: CardDto,
    val cardsInSideboard: List<CardDto> = emptyList(),
)
