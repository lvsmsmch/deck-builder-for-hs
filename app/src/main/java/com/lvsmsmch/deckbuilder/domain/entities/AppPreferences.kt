package com.lvsmsmch.deckbuilder.domain.entities

data class AppPreferences(
    val theme: ThemeMode = ThemeMode.System,
    val cardLocale: String = "en_US",
    val crashReportingEnabled: Boolean = true,
    val lastSeenSetSlug: String? = null,
)

enum class ThemeMode { System, Dark, Light }

object SupportedCardLocales {
    /** Battle.net-supported card locales. Plan §5.2. */
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
}
