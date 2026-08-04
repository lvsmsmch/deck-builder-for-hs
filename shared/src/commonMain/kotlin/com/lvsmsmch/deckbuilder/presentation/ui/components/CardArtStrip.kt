package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/**
 * Narrow slice of a card's artwork used as the leading edge of a list row.
 * The art fades into the row surface so the mana gem and the name stay
 * readable no matter how busy the illustration is; when there is no art the
 * class accent alone carries the row.
 */
@Composable
fun CardArtStrip(
    artUrl: String?,
    accent: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val surface = DeckBuilderColors.SurfaceContainer
    Box(modifier = modifier.fillMaxSize().alpha(alpha)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.55f), accent.copy(alpha = 0.06f)),
                        radius = 90f,
                    ),
                ),
        )
        if (!artUrl.isNullOrBlank()) {
            AsyncImage(
                model = artUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                // Card art puts the subject slightly above centre.
                alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.25f),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to surface.copy(alpha = 0.05f),
                        0.42f to surface.copy(alpha = 0.5f),
                        0.86f to surface,
                    ),
                ),
        )
    }
}
