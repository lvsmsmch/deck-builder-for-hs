package com.lvsmsmch.deckbuilder.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CardBackDto(
    val id: Int,
    val slug: String = "",
    val name: String = "",
    val text: String? = null,
    val image: String = "",
    val sortCategory: String? = null,
    val enabled: Int = 1,
)

@Serializable
data class CardBackSearchResponseDto(
    val cardBacks: List<CardBackDto> = emptyList(),
    val cardCount: Int = 0,
    val pageCount: Int = 0,
    val page: Int = 1,
)
