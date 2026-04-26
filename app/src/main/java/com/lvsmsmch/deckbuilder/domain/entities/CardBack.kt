package com.lvsmsmch.deckbuilder.domain.entities

data class CardBack(
    val id: Int,
    val slug: String,
    val name: String,
    val text: String?,
    val image: String,
    val sortCategory: String?,
    val enabled: Boolean,
)

object CardBackCategory {
    val All: String? = null

    /** Slugs accepted by the `cardBackCategory` query param. Plan §5.3. */
    val Known: List<Pair<String, String>> = listOf(
        "base" to "Base",
        "achieve" to "Achievement",
        "fireside" to "Fireside",
        "heroes" to "Heroes",
        "season" to "Seasonal",
        "legend" to "Legend",
        "esports" to "Esports",
        "game_license" to "Game license",
        "promotion" to "Promotion",
        "pre_purchase" to "Pre-purchase",
        "blizzard" to "Blizzard",
        "golden" to "Golden",
        "events" to "Events",
    )
}
