package com.lvsmsmch.deckbuilder.util

/** Localized "March 2021"-style label. */
expect fun formatMonthYear(year: Int, month: Int): String

/** Localized medium date + short time, e.g. "Jan 5, 2026, 2:41 PM". */
expect fun formatDateTime(epochMs: Long): String
