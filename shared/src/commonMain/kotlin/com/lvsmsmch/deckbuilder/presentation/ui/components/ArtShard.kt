package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/** The cut that gives every piece of art in the app its trailing edge. */
@Composable
fun shardShape(skew: Dp = 26.dp): Shape {
    val density = LocalDensity.current
    return remember(skew, density) {
        val cut = with(density) { skew.toPx() }
        GenericShape { size, _ ->
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - cut.coerceAtMost(size.width), size.height)
            lineTo(0f, size.height)
            close()
        }
    }
}

/**
 * Card art as an angled shard behind the start of a row: the class gradient
 * underneath, the illustration over it, then a wash back to the slab so the
 * crystal and the name stay readable however busy the art is.
 */
@Composable
fun ArtShard(
    artUrl: String?,
    gradient: Pair<Color, Color>,
    modifier: Modifier = Modifier,
    skew: Dp = 26.dp,
    alpha: Float = 1f,
    fadeFrom: Float = 0.18f,
    veil: Float = 0.45f,
) {
    ArtShard(
        gradient = gradient,
        modifier = modifier,
        skew = skew,
        alpha = alpha,
        fadeFrom = fadeFrom,
        veil = veil,
    ) {
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
    }
}

/** Same shard for art that isn't a plain URL — bundled hero portraits, mostly. */
@Composable
fun ArtShard(
    gradient: Pair<Color, Color>,
    modifier: Modifier = Modifier,
    skew: Dp = 26.dp,
    alpha: Float = 1f,
    fadeFrom: Float = 0.18f,
    /** How much of the slab is laid over the art. */
    veil: Float = 0.45f,
    art: @Composable () -> Unit,
) {
    val slab = DeckBuilderColors.Surface
    // On the porcelain ground the art competes with the text sitting over it,
    // so it steps back; on the dark slab it can hold its own.
    val ground = if (DeckBuilderColors.IsDark) alpha else alpha * 0.5f
    val fadeStart = if (DeckBuilderColors.IsDark) fadeFrom else 0f
    Box(modifier = modifier.fillMaxSize().clip(shardShape(skew)).alpha(ground)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(listOf(gradient.first, gradient.second)),
                ),
        )
        art()
        // Real card art is far brighter than a flat class gradient, so a veil
        // of the slab keeps it a texture rather than a picture competing with
        // the name written across it.
        Box(modifier = Modifier.fillMaxSize().background(slab.copy(alpha = veil)))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        fadeStart to Color.Transparent,
                        0.96f to slab,
                    ),
                ),
        )
    }
}
