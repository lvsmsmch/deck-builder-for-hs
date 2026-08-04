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
 * Parallax. The interface owns no ground of its own: card art fills the screen
 * and these surfaces are light laid over it. Every "surface" here is an alpha,
 * so a pane borrows whatever colour is behind it.
 */
internal val DarkAppTokens = AppTokens(
    isDark = true,
    // Only ever seen before the art paints, or behind a heavy blur.
    surface = Color(0xFF0A0C12),
    // The bar glass — nav and sheets, the heaviest panes in the app.
    surfaceContainer = Color(0x1FFFFFFF),
    // The pane glass everything else is made of.
    surfaceContainerHigh = Color(0x12FFFFFF),
    surfaceContainerHighest = Color(0x2EFFFFFF),
    // Edges are light, never a colour.
    outline = Color(0x29FFFFFF),
    outlineSoft = Color(0x17FFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceDim = Color(0xB3FFFFFF),
    onSurfaceDimmer = Color(0x70FFFFFF),
    // Amber: the one thing you can act on.
    primary = Color(0xFFFFB65C),
    onPrimary = Color(0xFF241503),
    primarySoft = Color(0x24FFB65C),
    // Blue: mana, and nothing else.
    mana = Color(0xFF7FD0FF),
    manaDim = Color(0x4D7FD0FF),
    secondary = Color(0xFFCE9450),
    error = Color(0xFFFF8A8A),
    success = Color(0xFF6EE7A8),
)

/** Daylight does not change the idea, only the light: the art is veiled in
 *  white instead of black and the glass frosts pale. */
internal val LightAppTokens = AppTokens(
    isDark = false,
    surface = Color(0xFFE9EDF3),
    surfaceContainer = Color(0xC2FFFFFF),
    surfaceContainerHigh = Color(0x9EFFFFFF),
    surfaceContainerHighest = Color(0xE0FFFFFF),
    outline = Color(0x1F181C2C),
    outlineSoft = Color(0x12181C2C),
    onSurface = Color(0xFF13161F),
    onSurfaceDim = Color(0xAD13161F),
    onSurfaceDimmer = Color(0x6B13161F),
    primary = Color(0xFF8A5312),
    onPrimary = Color(0xFFFFF4E4),
    primarySoft = Color(0x1F8A5312),
    mana = Color(0xFF0E63AE),
    manaDim = Color(0x520E63AE),
    secondary = Color(0xFF9A6A22),
    error = Color(0xFFA33338),
    success = Color(0xFF14764C),
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
    /**
     * Class art. Four stops each: ground, two lights and a floor, layered as
     * radial gradients into something that reads as an illustration behind
     * glass. Real card art replaces this wherever we have it.
     */
    data class Atmosphere(
        val ground: Color,
        val light: Color,
        val highlight: Color,
        val floor: Color,
    )

    object Class {
        val DeathKnight = Atmosphere(Color(0xFF0B2733), Color(0xFF2F8FA8), Color(0xFF7FE3F5), Color(0xFF06202C))
        val DemonHunter = Atmosphere(Color(0xFF152608), Color(0xFF5FA82E), Color(0xFFC4F49A), Color(0xFF0B1704))
        val Druid = Atmosphere(Color(0xFF2A1F0B), Color(0xFF9A7434), Color(0xFFE6C88A), Color(0xFF171003))
        val Hunter = Atmosphere(Color(0xFF12220E), Color(0xFF4E8C4A), Color(0xFFB6E39C), Color(0xFF0A1607))
        val Mage = Atmosphere(Color(0xFF0C2036), Color(0xFF2F74B8), Color(0xFF9FD6FF), Color(0xFF061524))
        val Paladin = Atmosphere(Color(0xFF3A2A08), Color(0xFFC99A2A), Color(0xFFFFE9A8), Color(0xFF231903))
        val Priest = Atmosphere(Color(0xFF232935), Color(0xFF7E8CA6), Color(0xFFEAF1FA), Color(0xFF161A22))
        val Rogue = Atmosphere(Color(0xFF14161C), Color(0xFF4A5162), Color(0xFFA8B0C4), Color(0xFF0A0B0F))
        val Shaman = Atmosphere(Color(0xFF0B1B36), Color(0xFF2A5FBF), Color(0xFF8FB6FF), Color(0xFF061024))
        val Warlock = Atmosphere(Color(0xFF1F1034), Color(0xFF6B3FBF), Color(0xFFC4A0FF), Color(0xFF130A20))
        val Warrior = Atmosphere(Color(0xFF320F0C), Color(0xFFB23A2A), Color(0xFFFF9A78), Color(0xFF1D0705))
        val Neutral = Atmosphere(Color(0xFF171A22), Color(0xFF4C5464), Color(0xFF9AA3B5), Color(0xFF0D0F14))

        fun of(slug: String?): Atmosphere = when (slug?.lowercase()) {
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

        fun accent(slug: String?): Color = of(slug).highlight
    }

    /** Rarity dots. Legendary borrows the action colour — it is the one card
     *  rarity people actually hunt for. */
    object Rarity {
        val Common = Color(0xFF9AA3B5)
        val Rare = Color(0xFF6AA8FF)
        val Epic = Color(0xFFC79BFF)
        val Legendary = Color(0xFFFFB65C)
    }
}
