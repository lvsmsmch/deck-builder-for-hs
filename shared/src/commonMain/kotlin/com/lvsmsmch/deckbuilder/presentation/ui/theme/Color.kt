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
    val isDark: Boolean,
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

/**
 * Warm stone neutrals with a cool mana-crystal accent — blue gems on a wooden
 * board, the game's own material pairing. Gold is reserved for legendaries and
 * Wild, never for decoration.
 */
internal val DarkAppTokens = AppTokens(
    isDark = true,
    surface = Color(0xFF131009),
    surfaceContainer = Color(0xFF1E1811),
    surfaceContainerHigh = Color(0xFF272016),
    surfaceContainerHighest = Color(0xFF31281B),
    outline = Color(0xFF362C1E),
    outlineSoft = Color(0xFF2C2418),
    onSurface = Color(0xFFF3EADA),
    onSurfaceDim = Color(0xFFA9997F),
    onSurfaceDimmer = Color(0xFF7C6F5C),
    primary = Color(0xFF6FB6FF),
    onPrimary = Color(0xFF0A1622),
    primarySoft = Color(0x296FB6FF),
    secondary = Color(0xFFE3AF5C),
    error = Color(0xFFE2705F),
    success = Color(0xFF6BC08A),
)

internal val LightAppTokens = AppTokens(
    isDark = false,
    surface = Color(0xFFEBE5DA),
    surfaceContainer = Color(0xFFFDFBF7),
    surfaceContainerHigh = Color(0xFFF3EDE3),
    surfaceContainerHighest = Color(0xFFE8E0D2),
    outline = Color(0xFFDCD3C4),
    outlineSoft = Color(0xFFE7E0D3),
    onSurface = Color(0xFF1E1811),
    onSurfaceDim = Color(0xFF6B6053),
    onSurfaceDimmer = Color(0xFF948877),
    primary = Color(0xFF1D6FC4),
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0x1F1D6FC4),
    secondary = Color(0xFFA8752A),
    error = Color(0xFFB23A2E),
    success = Color(0xFF2F7D4F),
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
    val IsDark: Boolean
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.isDark
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
        val Druid = Color(0xFFC08A4E)
        val Hunter = Color(0xFF7FA65B)
        val Mage = Color(0xFF5B92D6)
        val Paladin = Color(0xFFE0BA5C)
        val Priest = Color(0xFFC9CBD0)
        val Rogue = Color(0xFF9AA0A8)
        val Shaman = Color(0xFF4E7FBF)
        val Warlock = Color(0xFF9A72C4)
        val Warrior = Color(0xFFC0705A)
        val DemonHunter = Color(0xFF79B04F)
        val DeathKnight = Color(0xFF7FA9C9)
        val Neutral = Color(0xFF8C8172)
    }

    /** Rarity palette — also constant; the gem colors are universally recognised. */
    object Rarity {
        val Common = Color(0xFF9AA1AB)
        val Rare = Color(0xFF4E8FE0)
        val Epic = Color(0xFFA46BD8)
        val Legendary = Color(0xFFD79A3C)
    }
}
