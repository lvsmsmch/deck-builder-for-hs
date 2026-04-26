package com.lvsmsmch.deckbuilder.data.crash

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Thin wrapper over [FirebaseCrashlytics]. The class deliberately tolerates a
 * missing `google-services.json` — without that file Firebase's auto-init
 * fails and `FirebaseCrashlytics.getInstance()` throws. We swallow that once
 * at construction and then no-op on every public method.
 */
class CrashReporter(
    private val prefs: PreferencesRepository,
    private val scope: CoroutineScope,
) {
    private val crashlytics: FirebaseCrashlytics? =
        runCatching { FirebaseCrashlytics.getInstance() }
            .onFailure { Log.i(TAG, "Crashlytics unavailable (${it.message}); reporter is a no-op") }
            .getOrNull()

    val isAvailable: Boolean get() = crashlytics != null

    /** Mirrors the user toggle into the SDK. Safe to call on every app start. */
    fun bindToPreferences() {
        prefs.preferences
            .map { it.crashReportingEnabled }
            .distinctUntilChanged()
            .onEach { enabled -> crashlytics?.setCrashlyticsCollectionEnabled(enabled) }
            .launchIn(scope)
    }

    fun recordException(t: Throwable) {
        crashlytics?.recordException(t)
    }

    fun log(message: String) {
        crashlytics?.log(message)
    }

    private companion object {
        const val TAG = "CrashReporter"
    }
}
