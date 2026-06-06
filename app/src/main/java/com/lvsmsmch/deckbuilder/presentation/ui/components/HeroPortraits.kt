package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.lvsmsmch.deckbuilder.R
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

private val localHeroArt = mapOf(
    "HERO_01" to R.drawable.hero_warrior,
    "HERO_02" to R.drawable.hero_shaman,
    "HERO_03" to R.drawable.hero_rogue,
    "HERO_04" to R.drawable.hero_paladin,
    "HERO_05" to R.drawable.hero_hunter,
    "HERO_06" to R.drawable.hero_druid,
    "HERO_07" to R.drawable.hero_warlock,
    "HERO_08" to R.drawable.hero_mage,
    "HERO_09" to R.drawable.hero_priest,
    "HERO_10" to R.drawable.hero_demonhunter,
    "HERO_11" to R.drawable.hero_deathknight,
)

private fun heroArtModel(cardId: String): Any =
    localHeroArt[cardId] ?: "$ART_BASE/512x/$cardId.webp"

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
    zoomed: Boolean = false,
) {
    Box(modifier = modifier.background(fallbackTint)) {
        if (cardId != null) {
            AsyncImage(
                model = heroArtModel(cardId),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = if (zoomed) BiasAlignment(0f, -0.2f) else Alignment.Center,
            )
        }
    }
}

/** Horizontal hero art strip. Used in the saved-deck row + DeckView header. */
@Composable
fun HeroTile(
    cardId: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    verticalFocus: Float = 0.30f,
) {
    val yBias = (verticalFocus.coerceIn(0f, 1f) * 2f) - 1f
    Box(modifier = modifier.background(DeckBuilderColors.SurfaceContainer)) {
        if (cardId != null) {
            AsyncImage(
                model = heroArtModel(cardId),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(horizontalBias = 0f, verticalBias = yBias),
            )
        }
    }
}
