package com.lvsmsmch.deckbuilder.presentation.platform

/** Short user-visible notification (Android Toast; iOS falls back to console/log). */
interface Toaster {
    fun show(message: String)
}

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
