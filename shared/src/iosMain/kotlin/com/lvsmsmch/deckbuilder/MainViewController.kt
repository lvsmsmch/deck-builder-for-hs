package com.lvsmsmch.deckbuilder

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.lvsmsmch.deckbuilder.di.commonDataModule
import com.lvsmsmch.deckbuilder.di.domainModule
import com.lvsmsmch.deckbuilder.di.iosPlatformModule
import com.lvsmsmch.deckbuilder.di.presentationModule
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.presentation.DeckBuilderRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.mp.KoinPlatform
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

private var koinStarted = false
private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

fun initKoin() {
    if (koinStarted) return
    koinStarted = true
    startKoin {
        modules(
            iosPlatformModule,
            commonDataModule,
            domainModule,
            presentationModule,
        )
    }
}

/**
 * [setCrashCollectionEnabled] comes from Swift and flips Crashlytics'
 * collection flag; Kotlin mirrors the user's "Send error reports" toggle
 * into it, matching CrashReporter.bindToPreferences on Android.
 */
@Suppress("FunctionName", "unused")
fun MainViewController(
    setCrashCollectionEnabled: ((Boolean) -> Unit)? = null,
): UIViewController {
    initKoin()
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
    if (setCrashCollectionEnabled != null) {
        val prefs: PreferencesRepository = KoinPlatform.getKoin().get()
        prefs.preferences
            .map { it.crashReportingEnabled }
            .distinctUntilChanged()
            .onEach { enabled -> setCrashCollectionEnabled(enabled) }
            .launchIn(appScope)
    }
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    return ComposeUIViewController { DeckBuilderRoot() }
}
