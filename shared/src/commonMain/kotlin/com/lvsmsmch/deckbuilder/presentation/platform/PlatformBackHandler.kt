package com.lvsmsmch.deckbuilder.presentation.platform

import androidx.compose.runtime.Composable

/** System back interception: Android's BackHandler; no-op on iOS (no system back). */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
