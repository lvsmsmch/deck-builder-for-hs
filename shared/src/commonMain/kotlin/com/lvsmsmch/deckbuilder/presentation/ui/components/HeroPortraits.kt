package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.Image
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
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.hero_deathknight
import com.lvsmsmch.deckbuilder.resources.hero_demonhunter
import com.lvsmsmch.deckbuilder.resources.hero_druid
import com.lvsmsmch.deckbuilder.resources.hero_hunter
import com.lvsmsmch.deckbuilder.resources.hero_mage
import com.lvsmsmch.deckbuilder.resources.hero_paladin
import com.lvsmsmch.deckbuilder.resources.hero_priest
import com.lvsmsmch.deckbuilder.resources.hero_rogue
import com.lvsmsmch.deckbuilder.resources.hero_shaman
import com.lvsmsmch.deckbuilder.resources.hero_warlock
import com.lvsmsmch.deckbuilder.resources.hero_warrior
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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

    /**
     * dbfId for the canonical default hero of [classSlug], used as deck `hero`.
     * Verified against HearthstoneJSON: HERO_01..HERO_11 dbfIds. (Shaman/hunter
     * were once swapped here — Thrall is 1066, Rexxar is 31 — which silently
     * turned saved shaman decks into hunter decks.)
     */
    private val dbfIdByClassSlug = mapOf(
        "warrior" to 7,
        "shaman" to 1066,
        "rogue" to 930,
        "paladin" to 671,
        "hunter" to 31,
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

private val localHeroArt: Map<String, DrawableResource> = mapOf(
    "HERO_01" to Res.drawable.hero_warrior,
    "HERO_02" to Res.drawable.hero_shaman,
    "HERO_03" to Res.drawable.hero_rogue,
    "HERO_04" to Res.drawable.hero_paladin,
    "HERO_05" to Res.drawable.hero_hunter,
    "HERO_06" to Res.drawable.hero_druid,
    "HERO_07" to Res.drawable.hero_warlock,
    "HERO_08" to Res.drawable.hero_mage,
    "HERO_09" to Res.drawable.hero_priest,
    "HERO_10" to Res.drawable.hero_demonhunter,
    "HERO_11" to Res.drawable.hero_deathknight,
)

/** Bundled art for canonical heroes; remote HearthstoneJSON art otherwise. */
@Composable
private fun HeroArtImage(
    cardId: String,
    contentDescription: String?,
    alignment: Alignment,
) {
    val local = localHeroArt[cardId]
    if (local != null) {
        Image(
            painter = painterResource(local),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = alignment,
        )
    } else {
        AsyncImage(
            model = "$ART_BASE/512x/$cardId.webp",
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = alignment,
        )
    }
}

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
            HeroArtImage(
                cardId = cardId,
                contentDescription = contentDescription,
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
            HeroArtImage(
                cardId = cardId,
                contentDescription = contentDescription,
                alignment = BiasAlignment(horizontalBias = 0f, verticalBias = yBias),
            )
        }
    }
}
