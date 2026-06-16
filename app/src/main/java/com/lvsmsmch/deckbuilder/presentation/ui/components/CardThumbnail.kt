package com.lvsmsmch.deckbuilder.presentation.ui.components

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.data.debug.SessionLog
import com.lvsmsmch.deckbuilder.presentation.ui.theme.DeckBuilderColors
import org.koin.compose.koinInject

private const val CARD_ASPECT = 2f / 3f

/**
 * Hearthstone's full-card render is portrait (~0.72 aspect) and already
 * carries the cost, name, stats, and rarity gem. We don't decorate it — no
 * gradient frame, no border. While the image is loading we show a compact
 * text fallback so the pool stays readable even when remote art is slow.
 */
@Composable
fun CardThumbnail(
    card: Card,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val art = card.image.takeIf { it.isNotBlank() } ?: card.cropImage
    val context = LocalContext.current
    val sessionLog: SessionLog = koinInject()
    val started = remember(art) { SystemClock.elapsedRealtime() }
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(art)
            .size(256, 384)
            .build(),
    )
    val state by painter.state.collectAsState()
    LaunchedEffect(art) {
        sessionLog.add("Image.Card", "request id=${card.id} url=$art decode=256x356")
    }
    LaunchedEffect(state) {
        when (state) {
            is AsyncImagePainter.State.Success ->
                sessionLog.add("Image.Card", "loaded id=${card.id} ms=${SystemClock.elapsedRealtime() - started}")
            is AsyncImagePainter.State.Error ->
                sessionLog.add("Image.Card", "FAILED id=${card.id} url=$art ms=${SystemClock.elapsedRealtime() - started}")
            else -> Unit
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT)
            .clip(RoundedCornerShape(14.dp))
            .background(DeckBuilderColors.SurfaceContainer)
            .clickable(onClick = onClick),
    ) {
        CardImageFallback(card = card)
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

@Composable
private fun BoxScope.CardImageFallback(card: Card) {
    ManaGem(
        cost = card.manaCost,
        size = 34.dp,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(9.dp),
    )
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(12.dp),
    ) {
        Text(
            text = card.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = DeckBuilderColors.OnSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = listOfNotNull(card.rarity?.name, card.cardType.name.takeIf { it.isNotBlank() })
                .joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = DeckBuilderColors.OnSurfaceDim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
