package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

private const val ART_BASE = "https://art.hearthstonejson.com/v1"

/** Builds the HearthstoneJSON clean 512x card-art URL for a card slug. */
fun cardArtUrl(slug: String?): String? =
    slug?.takeIf { it.isNotBlank() }?.let { "$ART_BASE/512x/$it.webp" }

/** Kept for older call sites/tests that still use the tile naming. */
fun tileUrl(slug: String?): String? =
    cardArtUrl(slug)

/**
 * Horizontal card art strip. We crop from clean 512x art instead of using
 * HearthstoneJSON's pre-cut tiles, because those can contain odd repeated
 * fragments near the left edge for some cards.
 */
@Composable
fun CardTile(
    slug: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    verticalFocus: Float = 0.30f,
) {
    val url = cardArtUrl(slug)
    val yBias = (verticalFocus.coerceIn(0f, 1f) * 2f) - 1f
    Box(modifier = modifier.background(DeckBuilderColors.SurfaceContainer)) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.67f
                        scaleY = 1.67f
                    },
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(horizontalBias = 0f, verticalBias = yBias),
            )
        }
    }
}
