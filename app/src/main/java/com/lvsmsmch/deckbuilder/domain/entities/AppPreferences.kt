package com.lvsmsmch.deckbuilder.domain.entities

import java.util.Locale

data class AppPreferences(
    val theme: ThemeMode = ThemeMode.System,
    val cardLocale: String = SupportedCardLocales.defaultForSystem(),
    val crashReportingEnabled: Boolean = true,
    val lastSeenSetSlug: String? = null,
    val lastUpdateCheckAtMs: Long? = null,
)

enum class ThemeMode { System, Dark, Light }

object SupportedCardLocales {
    /** Hearthstone card locales supported by HearthstoneJSON. */
    val codes: List<Pair<String, String>> = listOf(
        "en_US" to "English (US)",
        "de_DE" to "Deutsch",
        "es_ES" to "Español (España)",
        "es_MX" to "Español (México)",
        "fr_FR" to "Français",
        "it_IT" to "Italiano",
        "ja_JP" to "日本語",
        "ko_KR" to "한국어",
        "pl_PL" to "Polski",
        "pt_BR" to "Português (BR)",
        "ru_RU" to "Русский",
        "th_TH" to "ภาษาไทย",
        "zh_TW" to "繁體中文",
    )

    fun isSupported(code: String): Boolean = codes.any { it.first == code }
    fun displayName(code: String): String = codes.firstOrNull { it.first == code }?.second ?: code

    fun defaultForSystem(locale: Locale = Locale.getDefault()): String {
        val exact = "${locale.language}_${locale.country}"
        return codes.firstOrNull { it.first.equals(exact, ignoreCase = true) }?.first
            ?: codes.firstOrNull { it.first.substringBefore('_').equals(locale.language, ignoreCase = true) }?.first
            ?: "en_US"
    }
}
