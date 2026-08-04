package com.lvsmsmch.deckbuilder.util

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.preferredLanguages

actual fun systemLanguage(): String = NSLocale.currentLocale.languageCode.lowercase()
actual fun systemCountry(): String = (NSLocale.currentLocale.countryCode ?: "").uppercase()

actual fun uiLanguage(): String =
    (NSLocale.preferredLanguages.firstOrNull() as? String)
        ?.substringBefore('-')
        ?.lowercase()
        ?: systemLanguage()
