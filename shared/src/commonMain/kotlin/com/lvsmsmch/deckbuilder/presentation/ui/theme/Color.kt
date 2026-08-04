package com.lvsmsmch.deckbuilder.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware token bag. The current bag is provided via [LocalAppTokens] in
 * [DeckBuilderTheme]; call sites read through [DeckBuilderColors], which
 * resolves each property at composition time.
 */
data class AppTokens(
    val isDark: Boolean,
    /** Screen ground. Cards sit on top of it, so it is the darkest step. */
    val surface: Color,
    /** Bars — bottom nav, action bars, the deck strip. */
    val surfaceContainer: Color,
    /** The card itself: every row, panel and group is one of these. */
    val surfaceContainerHigh: Color,
    /** Raised inside a card: search fields, the active segment. */
    val surfaceContainerHighest: Color,
    /** Hairline. Structure is drawn with one-pixel rules, never with shadows. */
    val outline: Color,
    val outlineSoft: Color,
    val onSurface: Color,
    val onSurfaceDim: Color,
    val onSurfaceDimmer: Color,
    /** Brass: the one thing on screen you can act on. */
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    /** Mana blue: cost, and nothing else, anywhere. */
    val mana: Color,
    val manaDim: Color,
    val secondary: Color,
    val error: Color,
    val success: Color,
)

/**
 * Nightforge. Neutrals are pulled toward blue rather than left grey, so the
 * card art — the only saturated thing in the app — reads as the warm element.
 * Two colours carry meaning and never drift: brass is action, blue is mana.
 */
internal val DarkAppTokens = AppTokens(
    isDark = true,
    surface = Color(0xFF0B0F17),
    surfaceContainer = Color(0xFF080B12),
    surfaceContainerHigh = Color(0xFF161C29),
    surfaceContainerHighest = Color(0xFF1F2736),
    outline = Color(0xFF2B3446),
    outlineSoft = Color(0xFF1E2534),
    onSurface = Color(0xFFE8EBF3),
    onSurfaceDim = Color(0xFF8A93A9),
    onSurfaceDimmer = Color(0xFF5A6379),
    primary = Color(0xFFE0A63C),
    onPrimary = Color(0xFF14100A),
    primarySoft = Color(0x1FE0A63C),
    mana = Color(0xFF59B8FF),
    manaDim = Color(0xFF1E3A57),
    secondary = Color(0xFF8A6620),
    error = Color(0xFFE4585B),
    success = Color(0xFF4FBF8B),
)

/**
 * Daylight: a slate-blue ground with pale cards laid on it. Deliberately not a
 * white page — the cards have to be the light thing in the room.
 */
internal val LightAppTokens = AppTokens(
    isDark = false,
    surface = Color(0xFFD5DBE6),
    surfaceContainer = Color(0xFFC7CEDC),
    surfaceContainerHigh = Color(0xFFF1F4FA),
    surfaceContainerHighest = Color(0xFFE3E8F1),
    outline = Color(0xFFB3BCCC),
    outlineSoft = Color(0xFFCFD6E2),
    onSurface = Color(0xFF10151F),
    onSurfaceDim = Color(0xFF4E586B),
    onSurfaceDimmer = Color(0xFF7C8698),
    primary = Color(0xFF96660F),
    onPrimary = Color(0xFFFFF8EC),
    primarySoft = Color(0x1A96660F),
    mana = Color(0xFF0F6FCC),
    manaDim = Color(0xFFC5DCF3),
    secondary = Color(0xFFC9A55E),
    error = Color(0xFFB8383B),
    success = Color(0xFF1B7F55),
)

/** Default to dark — Theme.kt overrides at the Composable boundary. */
val LocalAppTokens = staticCompositionLocalOf { DarkAppTokens }

/** Theme-aware accessor matching the `DeckBuilderColors.X` call shape. */
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

    /** Brass. Reach for this only where a tap does something. */
    val Primary: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.primary
    val OnPrimary: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.onPrimary
    val PrimarySoft: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.primarySoft

    /** Mana blue. Cost only — crystals, curves, the mana filter row. */
    val Mana: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.mana
    val ManaDim: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.manaDim

    val Secondary: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.secondary
    val Error: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.error
    val Success: Color
        @Composable @ReadOnlyComposable get() = LocalAppTokens.current.success

    /**
     * Class identity is carried by art, so every class is a two-stop gradient
     * rather than a flat chip colour. [Class.of] returns the pair; [Class.accent]
     * is the lighter stop, for the rare place that needs a single colour.
     */
    object Class {
        val DeathKnight = Color(0xFF1D4F5E) to Color(0xFF4FB0C8)
        val DemonHunter = Color(0xFF2A4A16) to Color(0xFF6FBF3A)
        val Druid = Color(0xFF4A3517) to Color(0xFFB98C4A)
        val Hunter = Color(0xFF1F3D1C) to Color(0xFF4E8C4A)
        val Mage = Color(0xFF173A5A) to Color(0xFF4FA3E3)
        val Paladin = Color(0xFF6B4E12) to Color(0xFFF2C94C)
        val Priest = Color(0xFF4A5566) to Color(0xFFDCE4EE)
        val Rogue = Color(0xFF22262E) to Color(0xFF79839A)
        val Shaman = Color(0xFF12325E) to Color(0xFF2A6FD6)
        val Warlock = Color(0xFF3B2160) to Color(0xFF8B5CF6)
        val Warrior = Color(0xFF5A1E17) to Color(0xFFC0392B)
        val Neutral = Color(0xFF232A3A) to Color(0xFF5D6678)

        fun of(slug: String?): Pair<Color, Color> = when (slug?.lowercase()) {
            "deathknight", "death-knight" -> DeathKnight
            "demonhunter", "demon-hunter" -> DemonHunter
            "druid" -> Druid
            "hunter" -> Hunter
            "mage" -> Mage
            "paladin" -> Paladin
            "priest" -> Priest
            "rogue" -> Rogue
            "shaman" -> Shaman
            "warlock" -> Warlock
            "warrior" -> Warrior
            else -> Neutral
        }

        fun accent(slug: String?): Color = of(slug).second
    }

    /** Rarity dots — constant across themes; the gem colours are universal. */
    object Rarity {
        val Common = Color(0xFF7E8798)
        val Rare = Color(0xFF3B82F6)
        val Epic = Color(0xFFA855F7)
        val Legendary = Color(0xFFF0A020)
    }
}
