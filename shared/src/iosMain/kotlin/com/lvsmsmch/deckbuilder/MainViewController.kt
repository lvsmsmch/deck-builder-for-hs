package com.lvsmsmch.deckbuilder

import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.lvsmsmch.deckbuilder.di.commonDataModule
import com.lvsmsmch.deckbuilder.di.domainModule
import com.lvsmsmch.deckbuilder.di.iosPlatformModule
import com.lvsmsmch.deckbuilder.di.presentationModule
import com.lvsmsmch.deckbuilder.presentation.DeckBuilderRoot
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

private var koinStarted = false

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

@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    initKoin()
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    return ComposeUIViewController { DeckBuilderRoot() }
}
