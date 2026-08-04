package com.lvsmsmch.deckbuilder.util

/** ISO 639 language code of the current system locale, lowercase (e.g. "ru"). */
expect fun systemLanguage(): String

/** ISO 3166 country code of the current system locale, uppercase (may be empty). */
expect fun systemCountry(): String

/**
 * ISO 639 code of the language the interface is actually drawn in, lowercase.
 * On iOS this is the preferred app language, which is what the string resources
 * follow and which the regional locale can disagree with.
 */
expect fun uiLanguage(): String
