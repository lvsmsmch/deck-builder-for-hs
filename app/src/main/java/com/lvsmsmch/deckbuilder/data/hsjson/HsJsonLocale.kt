package com.lvsmsmch.deckbuilder.data.hsjson

/**
 * Blizzard-style locales (`en_US`, `ru_RU`) → HearthstoneJSON form (`enUS`, `ruRU`).
 *
 * The app stores the Blizzard form in prefs (legacy from the metadata pipeline);
 * HsJson uses the camel-cased form in its URLs.
 */
fun blizzardLocaleToHsJson(locale: String): String =
    locale.split('_').let { parts ->
        if (parts.size == 2) parts[0].lowercase() + parts[1].uppercase()
        else locale.replace("_", "")
    }
