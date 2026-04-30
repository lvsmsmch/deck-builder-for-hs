package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

@Composable
fun ManaGem(
    cost: Int,
    size: Dp = 30.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    modifier: Modifier = Modifier,
) {
    StatGem(fill = StatGemPalette.Mana, size = size, modifier = modifier) {
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
