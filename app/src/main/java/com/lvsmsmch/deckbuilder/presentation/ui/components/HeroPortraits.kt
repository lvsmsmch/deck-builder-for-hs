package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/**
 * Canonical default-hero card IDs in HearthstoneJSON. Stable since launch:
 * Blizzard never reuses these slots even when alternate skins are added (those
 * get suffixes like `HERO_06p` for Wildheart Guff). Using these directly avoids
 * the "Druid shows Guff" bug that came from picking the first hero card with
 * `cardClass == DRUID`.
 */
object DefaultHeroes {
    private val byClassSlug = mapOf(
        "warrior" to "HERO_01",
        "shaman" to "HERO_02",
        "rogue" to "HERO_03",
        "paladin" to "HERO_04",
        "hunter" to "HERO_05",
        "druid" to "HERO_06",
        "warlock" to "HERO_07",
        "mage" to "HERO_08",
        "priest" to "HERO_09",
        "demonhunter" to "HERO_10",
        "deathknight" to "HERO_11",
    )

    fun cardIdFor(classSlug: String?): String? =
        classSlug?.lowercase()?.let(byClassSlug::get)

    /** dbfId for the canonical default hero of [classSlug], used as deck `hero`. */
    private val dbfIdByClassSlug = mapOf(
        "warrior" to 7,
        "shaman" to 31,
        "rogue" to 930,
        "paladin" to 671,
        "hunter" to 31127,
        "druid" to 274,
        "warlock" to 893,
        "mage" to 637,
        "priest" to 813,
        "demonhunter" to 56550,
        "deathknight" to 78065,
    )

    fun dbfIdFor(classSlug: String?): Int? =
        classSlug?.lowercase()?.let(dbfIdByClassSlug::get)
}

private const val ART_BASE = "https://art.hearthstonejson.com/v1"

/** 256×ratio render of the hero card. */
private fun heroRenderUrl(cardId: String, locale: String = "enUS"): String =
    "$ART_BASE/render/latest/$locale/256x/$cardId.png"

/** 256×59 horizontal tile. */
private fun heroTileUrl(cardId: String): String =
    "$ART_BASE/tiles/$cardId.png"

/**
 * Square-ish hero portrait used in the class picker. Falls back to a class-color
 * gradient when [cardId] is null (no canonical hero registered for the class).
 */
@Composable
fun HeroPortrait(
    cardId: String?,
    fallbackTint: Brush,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(fallbackTint)) {
        if (cardId != null) {
            AsyncImage(
                model = heroRenderUrl(cardId),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

/** 256×59 tile rendering of a hero. Used in the saved-deck row + DeckView header. */
@Composable
fun HeroTile(
    cardId: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(DeckBuilderColors.SurfaceContainer)) {
        if (cardId != null) {
            AsyncImage(
                model = heroTileUrl(cardId),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
