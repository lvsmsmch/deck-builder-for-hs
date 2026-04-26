package com.lvsmsmch.deckbuilder.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware token bag. The current bag is provided via [LocalAppTokens] in
 * [DeckBuilderTheme]. Existing call sites read through [DeckBuilderColors],
 * which now resolves each property at composition time — so adding light/dark
 * variants required no changes outside this file + Theme.kt.
 */
data class AppTokens(
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val outline: Color,
    val outlineSoft: Color,
    val onSurface: Color,
    val onSurfaceDim: Color,
    val onSurfaceDimmer: Color,
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val secondary: Color,
    val error: Color,
    val success: Color,
)

internal val DarkAppTokens = AppTokens(
    surface = Color(0xFF0B0B0E),
    surfaceContainer = Color(0xFF15161B),
    surfaceContainerHigh = Color(0xFF1E1F26),
    surfaceContainerHighest = Color(0xFF262830),
    outline = Color(0xFF2A2C34),
    outlineSoft = Color(0xFF23252C),
    onSurface = Color(0xFFECEDEF),
    onSurfaceDim = Color(0xFF9AA0A6),
    onSurfaceDimmer = Color(0xFF6B7178),
    primary = Color(0xFF7C8CFF),
    onPrimary = Color(0xFF0B0B0E),
    primarySoft = Color(0x227C8CFF),
    secondary = Color(0xFFFFB454),
    error = Color(0xFFFF6E6E),
    success = Color(0xFF5CC58A),
)

internal val LightAppTokens = AppTokens(
    surface = Color(0xFFF7F7FA),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF1F2F5),
    surfaceContainerHighest = Color(0xFFE7E9EE),
    outline = Color(0xFFD7DAE0),
    outlineSoft = Color(0xFFE3E5EA),
    onSurface = Color(0xFF111218),
    onSurfaceDim = Color(0xFF52575F),
    onSurfaceDimmer = Color(0xFF7A7E86),
    primary = Color(0xFF4F60E0),
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0x224F60E0),
    secondary = Color(0xFFB76C00),
    error = Color(0xFFC02525),
    success = Color(0xFF1F7B45),
)

/** Default to dark — Theme.kt overrides at the Composable boundary. */
val LocalAppTokens = staticCompositionLocalOf { DarkAppTokens }

/**
 * Theme-aware accessor matching the legacy `DeckBuilderColors.X` call shape.
 * Every property is `@Composable @ReadOnlyComposable` so the existing
 * `Modifier.background(DeckBuilderColors.Surface)` call sites continue to
 * compile and now switch automatically with the active theme.
 */
object DeckBuilderColors {
    val Surface: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.surface
    val SurfaceContainer: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.surfaceContainer
    val SurfaceContainerHigh: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.surfaceContainerHigh
    val SurfaceContainerHighest: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.surfaceContainerHighest
    val Outline: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.outline
    val OutlineSoft: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.outlineSoft
    val OnSurface: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.onSurface
    val OnSurfaceDim: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.onSurfaceDim
    val OnSurfaceDimmer: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.onSurfaceDimmer

    val Primary: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.primary
    val OnPrimary: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.onPrimary
    val PrimarySoft: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.primarySoft

    val Secondary: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.secondary
    val Error: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.error
    val Success: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.success

    /**
     * Class palette — intentionally constant across themes. The HS class colors
     * are part of brand identity and we want them recognisable regardless of theme.
     */
    object Class {
        val Druid = Color(0xFF9C7B4F)
        val Hunter = Color(0xFF5A6E3F)
        val Mage = Color(0xFF3F6CB5)
        val Paladin = Color(0xFFC9A24C)
        val Priest = Color(0xFFD6D6D6)
        val Rogue = Color(0xFF7A7A7A)
        val Shaman = Color(0xFF4A6C9D)
        val Warlock = Color(0xFF7E5BA8)
        val Warrior = Color(0xFFA05A45)
        val DemonHunter = Color(0xFF5C8E3D)
        val DeathKnight = Color(0xFF9E5C5C)
        val Neutral = Color(0xFF7A7A7A)
    }

    /** Rarity palette — also constant; the gem colors are universally recognised. */
    object Rarity {
        val Common = Color(0xFFB7BBC2)
        val Rare = Color(0xFF5BA6FF)
        val Epic = Color(0xFFB176FF)
        val Legendary = Color(0xFFFFC857)
    }
}
