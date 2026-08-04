package com.lvsmsmch.deckbuilder.util

import java.util.Locale

actual fun systemLanguage(): String = Locale.getDefault().language.lowercase()
actual fun systemCountry(): String = Locale.getDefault().country.uppercase()

actual fun uiLanguage(): String = Locale.getDefault().language.lowercase()
