package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clipToBounds
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

/**
 * The page itself. Card art fills the screen, goes soft, and a scrim darkens
 * it only where text will land — the top of the picture stays clean, the
 * bottom carries the words. Every screen sits on one of these; the app owns no
 * flat ground of its own.
 */
/**
 * The art layers of the current screen, published so a pane of glass can draw
 * the very thing behind it rather than a tint that pretends to.
 */
val LocalBackdropArt = staticCompositionLocalOf<(@Composable () -> Unit)?> { null }

/** The size of the surface that art was drawn at, so glass can line it up. */
val LocalBackdropSize = staticCompositionLocalOf { DpSize.Zero }

@Composable
fun Backdrop(
    atmosphere: DeckBuilderColors.Atmosphere,
    modifier: Modifier = Modifier,
    artUrl: String? = null,
    /** How hard the art is thrown out of focus. Zero leaves it sharp. */
    blurRadius: Dp = 44.dp,
    /** Where the scrim starts closing in. Lower means darker sooner. */
    scrimFrom: Float = 0.18f,
    /** For art that isn't a plain URL — the bundled hero portraits. */
    art: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = DeckBuilderColors.IsDark
    val hasArt = art != null || !artUrl.isNullOrBlank()
    val veilStart = if (isDark) Color(0x00070910) else Color(0x00F4F6FA)
    val veilMid = if (isDark) Color(0x94070910) else Color(0x99F4F6FA)
    val veilEnd = if (isDark) Color(0xDB070910) else Color(0xE0F4F6FA)

    val layers: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(atmosphere.ground),
        ) {
            when {
                art != null -> art()
                !artUrl.isNullOrBlank() -> AsyncImage(
                    model = artUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            // Painted light stands in for an illustration when there is none,
            // and warms one that is there.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(atmosphere.light.copy(alpha = if (hasArt) 0.35f else 0.85f), Color.Transparent),
                            center = Offset(0.22f * 1000f, 0.16f * 1000f),
                            radius = 780f,
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(atmosphere.highlight.copy(alpha = if (hasArt) 0.22f else 0.55f), Color.Transparent),
                            center = Offset(0.86f * 1000f, 0.24f * 1000f),
                            radius = 620f,
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.42f to Color.Transparent,
                            1f to atmosphere.floor,
                        ),
                    ),
            )
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(DeckBuilderColors.Surface)) {
        val screen = DpSize(maxWidth, maxHeight)
        Box(modifier = Modifier.fillMaxSize().blur(blurRadius)) { layers() }
        // The scrim is the only reason any of this stays readable.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        scrimFrom to veilStart,
                        0.52f to veilMid,
                        1f to veilEnd,
                    ),
                ),
        )
        CompositionLocalProvider(
            LocalBackdropArt provides layers,
            LocalBackdropSize provides screen,
        ) {
            content()
        }
    }
}

/**
 * Frosted glass that means it: the pane redraws the screen's own backdrop,
 * shifted by its position and thrown further out of focus, then lays a light
 * over it. Reserved for the pinned pieces — a bar, a sheet — where the eye
 * expects to see something through the glass.
 */
@Composable
fun FrostedSurface(
    modifier: Modifier = Modifier,
    tint: Color = DeckBuilderColors.SurfaceContainer,
    blurRadius: Dp = 30.dp,
    /**
     * How much of the ground goes under the tint. Zero where only the backdrop
     * is behind the glass; near one where real content passes beneath it and
     * would otherwise read straight through — Compose cannot blur what is
     * behind a composable, only what is inside it.
     */
    base: Float = 0f,
    content: @Composable BoxScope.() -> Unit,
) {
    val art = LocalBackdropArt.current
    val screen = LocalBackdropSize.current
    val density = LocalDensity.current
    var offset by remember { mutableStateOf(Offset.Zero) }
    // Measured rather than matched: a lazy list inside the pane settles its
    // height late, and a matchParentSize backing would stop short of it.
    var paneSize by remember { mutableStateOf(DpSize.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned { coords ->
            offset = coords.positionInRoot()
            paneSize = with(density) {
                DpSize(coords.size.width.toDp(), coords.size.height.toDp())
            }
        },
    ) {
        if (paneSize.width > 0.dp) {
            Box(modifier = Modifier.size(paneSize).clipToBounds()) {
                if (art != null && screen.width > 0.dp) {
                    Box(
                        modifier = Modifier
                            .size(screen)
                            .graphicsLayer {
                                translationX = -offset.x
                                translationY = -offset.y
                            }
                            .blur(blurRadius),
                    ) { art() }
                }
            }
            if (base > 0f) {
                Box(
                    modifier = Modifier
                        .size(paneSize)
                        .background(DeckBuilderColors.Surface.copy(alpha = base)),
                )
            }
            Box(modifier = Modifier.size(paneSize).background(tint))
        }
        content()
    }
}

/** A framed piece of the same art, for thumbnails and hero panels. */
@Composable
fun ArtPatch(
    atmosphere: DeckBuilderColors.Atmosphere,
    modifier: Modifier = Modifier,
    artUrl: String? = null,
    art: (@Composable () -> Unit)? = null,
) {
    Box(modifier = modifier.background(atmosphere.ground)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(atmosphere.light, atmosphere.highlight.copy(alpha = 0.75f)),
                    ),
                ),
        )
        when {
            art != null -> art()
            !artUrl.isNullOrBlank() -> AsyncImage(
                model = artUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
