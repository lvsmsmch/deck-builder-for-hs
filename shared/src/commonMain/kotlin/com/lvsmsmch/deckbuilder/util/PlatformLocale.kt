package com.lvsmsmch.deckbuilder.util

/** ISO 639 language code of the current system locale, lowercase (e.g. "ru"). */
expect fun systemLanguage(): String

/** ISO 3166 country code of the current system locale, uppercase (may be empty). */
expect fun systemCountry(): String
