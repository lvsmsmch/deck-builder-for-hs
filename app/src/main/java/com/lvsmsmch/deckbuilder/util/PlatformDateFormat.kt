package com.lvsmsmch.deckbuilder.util

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Locale-aware date rendering. Android/JVM implementation — becomes the
 * `actual` of an expect/actual pair in the KMP split (iOS: NSDateFormatter).
 */

/** Localized "March 2021"-style label. */
fun formatMonthYear(year: Int, month: Int, locale: Locale = Locale.getDefault()): String {
    val text = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
    return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

/** Localized medium date + short time, e.g. "Jan 5, 2026, 2:41 PM". */
fun formatDateTime(epochMs: Long): String {
    val fmt = java.text.DateFormat.getDateTimeInstance(
        java.text.DateFormat.MEDIUM,
        java.text.DateFormat.SHORT,
    )
    return fmt.format(java.util.Date(epochMs))
}
