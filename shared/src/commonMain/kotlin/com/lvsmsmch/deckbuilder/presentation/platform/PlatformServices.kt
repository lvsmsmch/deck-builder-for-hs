package com.lvsmsmch.deckbuilder.presentation.platform

enum class NetworkType { Wifi, Mobile, None }

interface NetworkMonitor {
    fun currentNetworkType(): NetworkType
}

/** Build metadata the UI shows in Settings; provided per platform via Koin. */
data class AppInfo(
    val versionName: String,
    val applicationId: String,
    val isDebug: Boolean,
)
