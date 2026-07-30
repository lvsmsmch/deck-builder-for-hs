package com.lvsmsmch.deckbuilder.util

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun systemLanguage(): String = NSLocale.currentLocale.languageCode.lowercase()
actual fun systemCountry(): String = (NSLocale.currentLocale.countryCode ?: "").uppercase()
