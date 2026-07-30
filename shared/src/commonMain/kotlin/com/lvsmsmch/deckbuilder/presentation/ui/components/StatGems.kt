package com.lvsmsmch.deckbuilder.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Hexagon-with-pointed-top gem shape — same silhouette the in-game UI uses for
 * mana, attack and health. [GemShape] points upward (sharp ridge at the top),
 * which mirrors the small ManaGem already used as a card-cost overlay.
 */
val GemShape: GenericShape = GenericShape { size, _ ->
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

object StatGemPalette {
    val Mana = Brush.verticalGradient(listOf(Color(0xFF7DB7FF), Color(0xFF1F4CC0)))
    val Attack = Brush.verticalGradient(listOf(Color(0xFFFF8A6E), Color(0xFFAD2020)))
    val Health = Brush.verticalGradient(listOf(Color(0xFFFF8888), Color(0xFFB42323)))
    val Armor = Brush.verticalGradient(listOf(Color(0xFFB4E58C), Color(0xFF2F7A2A)))
    val Weapon = Brush.verticalGradient(listOf(Color(0xFFE0C266), Color(0xFF8A6710)))
}

/** Solid gradient gem with no inner content — stat-row glyph. */
@Composable
fun StatGem(
    fill: Brush,
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier
            .size(width = size, height = size + 4.dp)
            .clip(GemShape)
            .background(fill),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Crossed-daggers icon inscribed inside [size]. We draw two short blades
 * forming an `X`, with thin guards near the hilt. Stroke width auto-scales
 * with [size].
 */
@Composable
fun CrossedDaggers(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.size(size),
    ) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.10f

        fun drawDagger(p: Path) {
            drawPath(p, color = color, style = Stroke(width = stroke, pathEffect = PathEffect.cornerPathEffect(stroke / 2f)))
        }

        // Blade A: top-left → bottom-right
        val a = Path().apply {
            moveTo(w * 0.18f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.82f)
        }
        drawDagger(a)
        // Blade B: top-right → bottom-left
        val b = Path().apply {
            moveTo(w * 0.82f, h * 0.18f)
            lineTo(w * 0.18f, h * 0.82f)
        }
        drawDagger(b)

        // Tiny guards perpendicular to each blade near the hilt (the lower end)
        val guard = w * 0.18f
        val perpA = Offset(-1f, 1f)
        val perpB = Offset(1f, 1f)
        fun perpUnit(o: Offset): Offset {
            val n = kotlin.math.hypot(o.x, o.y)
            return Offset(o.x / n, o.y / n)
        }

        val hiltA = Offset(w * 0.78f, h * 0.78f)
        val pA = perpUnit(perpA)
        drawLine(
            color = color,
            start = Offset(hiltA.x - pA.x * guard / 2, hiltA.y - pA.y * guard / 2),
            end = Offset(hiltA.x + pA.x * guard / 2, hiltA.y + pA.y * guard / 2),
            strokeWidth = stroke * 0.9f,
        )
        val hiltB = Offset(w * 0.22f, h * 0.78f)
        val pB = perpUnit(perpB)
        drawLine(
            color = color,
            start = Offset(hiltB.x - pB.x * guard / 2, hiltB.y - pB.y * guard / 2),
            end = Offset(hiltB.x + pB.x * guard / 2, hiltB.y + pB.y * guard / 2),
            strokeWidth = stroke * 0.9f,
        )
    }
}
