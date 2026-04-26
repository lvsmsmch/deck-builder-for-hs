package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.GenericShape

private val GemShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.5f, 0f)
    lineTo(w, h * 0.28f)
    lineTo(w, h * 0.72f)
    lineTo(w * 0.5f, h)
    lineTo(0f, h * 0.72f)
    lineTo(0f, h * 0.28f)
    close()
}

@Composable
fun ManaGem(
    cost: Int,
    size: Dp = 30.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = size, height = size + 4.dp)
            .clip(GemShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF5BA6FF), Color(0xFF1F4CC0)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cost.toString(),
            color = Color.White,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                shadow = Shadow(color = Color(0xCC000000), blurRadius = 2f),
            ),
        )
    }
}
