package com.lvsmsmch.deckbuilder.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.lvsmsmch.deckbuilder.domain.entities.ThemeMode

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

    CompositionLocalProvider(LocalAppTokens provides tokens) {
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
