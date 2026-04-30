package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors

private const val CARD_ASPECT = 0.72f

/**
 * Hearthstone's full-card render is portrait (~0.72 aspect) and already
 * carries the cost, name, stats, and rarity gem. We don't decorate it — no
 * gradient frame, no border. While the image is loading we show a flat
 * surface placeholder so the grid keeps its shape without flashing borders.
 */
@Composable
fun CardThumbnail(
    card: Card,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val art = card.image.takeIf { it.isNotBlank() } ?: card.cropImage
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context).data(art).build(),
    )
    val state by painter.state.collectAsState()

    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT)
            .clip(RoundedCornerShape(14.dp))
            .background(DeckBuilderColors.SurfaceContainer)
            .clickable(onClick = onClick),
    ) {
        if (art != null && state is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = card.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
