package com.lvsmsmch.deckbuilder.domain.entities

/** Stable representation of a query against `/hearthstone/cards`. */
data class CardFilters(
    val classes: Set<String> = emptySet(),
    val sets: Set<String> = emptySet(),
    val rarities: Set<String> = emptySet(),
    val types: Set<String> = emptySet(),
    val minionTypes: Set<String> = emptySet(),
    val keywords: Set<String> = emptySet(),
    val spellSchools: Set<String> = emptySet(),
    val manaCosts: Set<Int> = emptySet(),
    /**
     * Battlegrounds tier filter. Values are CSV-friendly tokens — "1".."6"
     * for the regular tiers and "hero" for hero portraits. Only used when
     * [gameMode] = BATTLEGROUNDS; ignored for constructed search.
     */
    val tiers: Set<String> = emptySet(),
    val gameMode: GameMode = GameMode.CONSTRUCTED,
    val collectibleOnly: Boolean = true,
    val textQuery: String = "",
    val sort: CardSort = CardSort(),
) {
    enum class GameMode(val apiSlug: String) {
        CONSTRUCTED("constructed"),
        BATTLEGROUNDS("battlegrounds"),
    }

    val hasFilters: Boolean
        get() = classes.isNotEmpty() ||
            sets.isNotEmpty() ||
            rarities.isNotEmpty() ||
            types.isNotEmpty() ||
            minionTypes.isNotEmpty() ||
            keywords.isNotEmpty() ||
            spellSchools.isNotEmpty() ||
            manaCosts.isNotEmpty() ||
            !collectibleOnly ||
            textQuery.isNotBlank()
}

data class CardSort(
    val key: SortKey = SortKey.MANA_COST,
    val direction: SortDir = SortDir.ASC,
) {
    /** API expects `manaCost:asc,attack:desc`. We keep one key — the API supports compound, we don't expose it. */
    fun toApiParam(): String = "${key.api}:${direction.api}"
}

enum class SortKey(val api: String) {
    MANA_COST("manaCost"),
    ATTACK("attack"),
    HEALTH("health"),
    NAME("name"),
    DATE_ADDED("dateAdded"),
    GROUP_BY_CLASS("groupByClass"),
}

enum class SortDir(val api: String) {
    ASC("asc"),
    DESC("desc"),
}
