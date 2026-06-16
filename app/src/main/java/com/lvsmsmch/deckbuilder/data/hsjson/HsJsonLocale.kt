package com.lvsmsmch.deckbuilder.data.hsjson

/** Converts app locale codes (`en_US`, `ru_RU`) to HearthstoneJSON URL form (`enUS`, `ruRU`). */
fun appLocaleToHsJson(locale: String): String =
    locale.split('_').let { parts ->
        if (parts.size == 2) parts[0].lowercase() + parts[1].uppercase()
        else locale.replace("_", "")
    }
