package com.lvsmsmch.deckbuilder.util

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

actual fun formatMonthYear(year: Int, month: Int): String {
    val locale = Locale.getDefault()
    val text = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
    return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

actual fun formatDateTime(epochMs: Long): String {
    val fmt = java.text.DateFormat.getDateTimeInstance(
        java.text.DateFormat.MEDIUM,
        java.text.DateFormat.SHORT,
    )
    return fmt.format(java.util.Date(epochMs))
}
