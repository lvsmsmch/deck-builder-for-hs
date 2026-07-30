package com.lvsmsmch.deckbuilder.presentation.ui.labels

import com.lvsmsmch.deckbuilder.util.formatMonthYear

/**
 * Release month per expansion. HearthstoneJSON's cards.json carries only the
 * set *code*, never a date, so this table is the single source of release
 * dates. A card's release date is its set's release date.
 *
 * Maintenance: add one line when Blizzard ships a new expansion (Mar/Jul/Nov
 * cadence). An unknown slug simply renders no date — nothing breaks.
 *
 * Rotating/virtual sets (core, legacy, vanilla, event, hero skins) are
 * intentionally absent: a "release date" is meaningless for them.
 */
object SetReleaseDates {

    private val bySlug: Map<String, Pair<Int, Int>> = mapOf(
        "naxx" to (2014 to 7),
        "gvg" to (2014 to 12),
        "brm" to (2015 to 4),
        "tgt" to (2015 to 8),
        "loe" to (2015 to 11),
        "og" to (2016 to 4),
        "kara" to (2016 to 8),
        "gangs" to (2016 to 12),
        "ungoro" to (2017 to 4),
        "icecrown" to (2017 to 8),
        "lootapalooza" to (2017 to 12),
        "gilneas" to (2018 to 4),
        "boomsday" to (2018 to 8),
        "troll" to (2018 to 12),
        "dalaran" to (2019 to 4),
        "uldum" to (2019 to 8),
        "dragons" to (2019 to 12),
        "year-of-the-dragon" to (2020 to 1),
        "demon-hunter-initiate" to (2020 to 4),
        "black-temple" to (2020 to 4),
        "scholomance" to (2020 to 8),
        "darkmoon-faire" to (2020 to 11),
        "the-barrens" to (2021 to 3),
        "stormwind" to (2021 to 8),
        "alterac-valley" to (2021 to 12),
        "the-sunken-city" to (2022 to 4),
        "revendreth" to (2022 to 8),
        "path-of-arthas" to (2022 to 12),
        "return-of-the-lich-king" to (2022 to 12),
        "battle-of-the-bands" to (2023 to 4),
        "titans" to (2023 to 8),
        "wonders" to (2023 to 8),
        "wild-west" to (2023 to 11),
        "whizbangs-workshop" to (2024 to 3),
        "island-vacation" to (2024 to 7),
        "space" to (2024 to 11),
        "emerald-dream" to (2025 to 3),
        "the-lost-city" to (2025 to 7),
        "cataclysm" to (2025 to 11),
        "escapefrom-violet-hold" to (2026 to 3),
        "time-travel" to (2026 to 7),
    )

    fun releaseMonthFor(setSlug: String?): Pair<Int, Int>? = setSlug?.let(bySlug::get)

    /** Localized "March 2021"-style label, or null when the set is unknown. */
    fun label(setSlug: String?): String? {
        val (year, month) = releaseMonthFor(setSlug) ?: return null
        return formatMonthYear(year, month)
    }
}
