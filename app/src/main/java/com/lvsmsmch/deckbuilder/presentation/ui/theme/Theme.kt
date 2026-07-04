package com.lvsmsmch.deckbuilder.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckBuilderTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
        ThemeMode.System -> isSystemInDarkTheme()
    }
    val tokens = if (isDark) DarkAppTokens else LightAppTokens
    val scheme = if (isDark) buildDarkScheme(tokens) else buildLightScheme(tokens)
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        window.statusBarColor = tokens.surface.toArgb()
        window.navigationBarColor = tokens.surface.toArgb()
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
    }
    val rippleConfiguration = RippleConfiguration(
        color = Color.White,
        rippleAlpha = RippleAlpha(
            pressedAlpha = 0.08f,
            focusedAlpha = 0f,
            draggedAlpha = 0f,
            hoveredAlpha = 0f,
        ),
    )

    CompositionLocalProvider(
        LocalAppTokens provides tokens,
        LocalRippleConfiguration provides rippleConfiguration,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = DeckBuilderTypography,
            shapes = DeckBuilderShapes,
            content = content,
        )
    }
}

private fun buildDarkScheme(t: AppTokens) = darkColorScheme(
    primary = t.primary,
    onPrimary = t.onPrimary,
    primaryContainer = t.primarySoft,
    onPrimaryContainer = t.onSurface,
    secondary = t.secondary,
    onSecondary = t.onPrimary,
    background = t.surface,
    onBackground = t.onSurface,
    surface = t.surface,
    onSurface = t.onSurface,
    surfaceVariant = t.surfaceContainer,
    onSurfaceVariant = t.onSurfaceDim,
    surfaceContainer = t.surfaceContainer,
    surfaceContainerHigh = t.surfaceContainerHigh,
    surfaceContainerHighest = t.surfaceContainerHighest,
    outline = t.outline,
    outlineVariant = t.outlineSoft,
    error = t.error,
    onError = t.onPrimary,
)

private fun buildLightScheme(t: AppTokens) = lightColorScheme(
    primary = t.primary,
    onPrimary = t.onPrimary,
    primaryContainer = t.primarySoft,
    onPrimaryContainer = t.onSurface,
    secondary = t.secondary,
    onSecondary = t.onPrimary,
    background = t.surface,
    onBackground = t.onSurface,
    surface = t.surface,
    onSurface = t.onSurface,
    surfaceVariant = t.surfaceContainer,
    onSurfaceVariant = t.onSurfaceDim,
    surfaceContainer = t.surfaceContainer,
    surfaceContainerHigh = t.surfaceContainerHigh,
    surfaceContainerHighest = t.surfaceContainerHighest,
    outline = t.outline,
    outlineVariant = t.outlineSoft,
    error = t.error,
    onError = t.onPrimary,
)
