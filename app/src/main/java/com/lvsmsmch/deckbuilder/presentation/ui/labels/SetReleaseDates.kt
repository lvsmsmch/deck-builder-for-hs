package com.lvsmsmch.deckbuilder.presentation.ui.labels

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    private val bySlug: Map<String, YearMonth> = mapOf(
        "naxx" to YearMonth.of(2014, 7),
        "gvg" to YearMonth.of(2014, 12),
        "brm" to YearMonth.of(2015, 4),
        "tgt" to YearMonth.of(2015, 8),
        "loe" to YearMonth.of(2015, 11),
        "og" to YearMonth.of(2016, 4),
        "kara" to YearMonth.of(2016, 8),
        "gangs" to YearMonth.of(2016, 12),
        "ungoro" to YearMonth.of(2017, 4),
        "icecrown" to YearMonth.of(2017, 8),
        "lootapalooza" to YearMonth.of(2017, 12),
        "gilneas" to YearMonth.of(2018, 4),
        "boomsday" to YearMonth.of(2018, 8),
        "troll" to YearMonth.of(2018, 12),
        "dalaran" to YearMonth.of(2019, 4),
        "uldum" to YearMonth.of(2019, 8),
        "dragons" to YearMonth.of(2019, 12),
        "year-of-the-dragon" to YearMonth.of(2020, 1),
        "demon-hunter-initiate" to YearMonth.of(2020, 4),
        "black-temple" to YearMonth.of(2020, 4),
        "scholomance" to YearMonth.of(2020, 8),
        "darkmoon-faire" to YearMonth.of(2020, 11),
        "the-barrens" to YearMonth.of(2021, 3),
        "stormwind" to YearMonth.of(2021, 8),
        "alterac-valley" to YearMonth.of(2021, 12),
        "the-sunken-city" to YearMonth.of(2022, 4),
        "revendreth" to YearMonth.of(2022, 8),
        "path-of-arthas" to YearMonth.of(2022, 12),
        "return-of-the-lich-king" to YearMonth.of(2022, 12),
        "battle-of-the-bands" to YearMonth.of(2023, 4),
        "titans" to YearMonth.of(2023, 8),
        "wonders" to YearMonth.of(2023, 8),
        "wild-west" to YearMonth.of(2023, 11),
        "whizbangs-workshop" to YearMonth.of(2024, 3),
        "island-vacation" to YearMonth.of(2024, 7),
        "space" to YearMonth.of(2024, 11),
        "emerald-dream" to YearMonth.of(2025, 3),
        "the-lost-city" to YearMonth.of(2025, 7),
        "cataclysm" to YearMonth.of(2025, 11),
        "escapefrom-violet-hold" to YearMonth.of(2026, 3),
        "time-travel" to YearMonth.of(2026, 7),
    )

    fun releaseMonthFor(setSlug: String?): YearMonth? = setSlug?.let(bySlug::get)

    /** Localized "March 2021"-style label, or null when the set is unknown. */
    fun label(setSlug: String?, locale: Locale = Locale.getDefault()): String? {
        val month = releaseMonthFor(setSlug) ?: return null
        val text = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
        return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }
}
