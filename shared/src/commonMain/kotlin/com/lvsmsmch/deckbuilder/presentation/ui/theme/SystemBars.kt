package com.lvsmsmch.deckbuilder.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Colors the OS bars on Android; no-op on iOS (bars are drawn by content). */
@Composable
expect fun ApplySystemBarsStyle(surface: Color, isDark: Boolean)
