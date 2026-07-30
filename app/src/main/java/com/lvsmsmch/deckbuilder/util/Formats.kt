package com.lvsmsmch.deckbuilder.util

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Locale-independent number formatting without JVM String.format, so these
 * helpers can live in a multiplatform source set. Decimal separator is always
 * '.', matching the previous Locale-default behaviour on this app's surfaces
 * (sizes in MB, average mana) closely enough.
 */
fun formatFixed(value: Double, decimals: Int): String {
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    val scaled = (value * factor).roundToLong()
    val intPart = scaled / factor
    if (decimals == 0) return intPart.toString()
    val frac = abs(scaled % factor).toString().padStart(decimals, '0')
    return "$intPart.$frac"
}

fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> "${formatFixed(bytes / (1024.0 * 1024.0), 1)} MB"
    bytes >= 1024L -> "${formatFixed(bytes / 1024.0, 1)} KB"
    else -> "$bytes B"
}

/** Whole megabytes above 10 MB, one decimal below — used by download progress. */
fun formatReadableMb(bytes: Long): String {
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return if (mb >= 10.0) "${formatFixed(mb, 0)} MB" else "${formatFixed(mb, 1)} MB"
}
