package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

private const val ART_BASE = "https://art.hearthstonejson.com/v1"

/** Builds the HearthstoneJSON 256×59 horizontal tile URL for a card slug. */
fun tileUrl(slug: String?): String? =
    slug?.takeIf { it.isNotBlank() }?.let { "$ART_BASE/tiles/$it.png" }

/**
 * Horizontal card tile (256×59 native). Caller controls size via [modifier].
 * Falls back to a flat surface fill when the slug is missing.
 */
@Composable
fun CardTile(
    slug: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillBounds,
) {
    val url = tileUrl(slug)
    Box(modifier = modifier.background(DeckBuilderColors.SurfaceContainer)) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        }
    }
}
