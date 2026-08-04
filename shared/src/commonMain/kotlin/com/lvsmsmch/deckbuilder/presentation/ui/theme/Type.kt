package com.lvsmsmch.deckbuilder.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lvsmsmch.deckbuilder.resources.Res
import com.lvsmsmch.deckbuilder.resources.spectral_bold
import com.lvsmsmch.deckbuilder.resources.spectral_medium
import com.lvsmsmch.deckbuilder.resources.spectral_semibold
import com.lvsmsmch.deckbuilder.resources.plex_mono_medium
import com.lvsmsmch.deckbuilder.resources.plex_mono_regular
import org.jetbrains.compose.resources.Font

/**
 * Three roles, and none of them borrows another's job.
 *
 * [display] — an old-style serif in wide capitals for screen titles, deck names
 * and the figures that matter. [body] is the platform face, because prose should look native.
 * [data] is monospaced so digits line up in a column: costs, counts, dust, dates.
 */
object AppType {

    val display: FontFamily
        @Composable get() = FontFamily(
            Font(Res.font.spectral_medium, FontWeight.Medium),
            Font(Res.font.spectral_semibold, FontWeight.SemiBold),
            Font(Res.font.spectral_bold, FontWeight.Bold),
        )

    val body: FontFamily get() = FontFamily.Default

    val data: FontFamily
        @Composable get() = FontFamily(
            Font(Res.font.plex_mono_regular, FontWeight.Normal),
            Font(Res.font.plex_mono_medium, FontWeight.Medium),
        )

    /** Screen titles: DECKS, LIBRARY, a deck's own name. */
    val screenTitle: TextStyle
        @Composable get() = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 27.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.2).sp,
        )

    val heroTitle: TextStyle
        @Composable get() = screenTitle.copy(fontSize = 30.sp, lineHeight = 34.sp)

    /** Deck names in a list. */
    val deckName: TextStyle
        @Composable get() = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.1).sp,
        )

    /** The three figures on a deck: cards, average mana, dust. */
    val figure: TextStyle
        @Composable get() = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 23.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.2.sp,
        )

    /** Tracked capitals: section headers, stat captions, chips. */
    val micro: TextStyle
        @Composable get() = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            letterSpacing = 2.0.sp,
        )

    val microSmall: TextStyle
        @Composable get() = micro.copy(fontSize = 9.5.sp, lineHeight = 13.sp, letterSpacing = 1.6.sp)

    val button: TextStyle
        @Composable get() = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 2.2.sp,
        )

    /** Mana numerals inside the crystal. */
    val gem: TextStyle
        @Composable get() = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 16.sp,
        )

    /** Every digit that sits in a column. */
    val mono: TextStyle
        @Composable get() = TextStyle(
            fontFamily = data,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )

    val monoSmall: TextStyle
        @Composable get() = mono.copy(fontSize = 10.5.sp, lineHeight = 14.sp)

    /** Card and row names — the platform face, at reading weight. */
    val rowName: TextStyle
        get() = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.5.sp,
            lineHeight = 18.sp,
            letterSpacing = (-0.05).sp,
        )

    val rowSub: TextStyle
        get() = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 14.sp,
        )
}

/**
 * Material's own scale still backs anything we haven't restyled by hand
 * (dialogs, menus, text fields), so it stays on the platform face.
 */
private val Sans: FontFamily = FontFamily.Default

val DeckBuilderTypography: Typography = Typography(
    displayLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.02).sp),
    displayMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.02).sp),
    headlineLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.01).sp),
    headlineMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.01).sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.06.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.08.sp),
)
