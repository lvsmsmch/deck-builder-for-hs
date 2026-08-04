package com.lvsmsmch.deckbuilder.di

import com.lvsmsmch.deckbuilder.data.db.AppDatabase
import com.lvsmsmch.deckbuilder.data.debug.SessionLog
import com.lvsmsmch.deckbuilder.data.hsjson.BuildChecker
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonApi
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonBuildStore
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonRepository
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.data.prefs.PreferencesRepositoryImpl
import com.lvsmsmch.deckbuilder.data.repository.CardRepositoryImpl
import com.lvsmsmch.deckbuilder.data.repository.DeckRepositoryImpl
import com.lvsmsmch.deckbuilder.data.repository.SavedDeckRepositoryImpl
import com.lvsmsmch.deckbuilder.data.rotation.RotationApi
import com.lvsmsmch.deckbuilder.data.rotation.RotationRepositoryImpl
import com.lvsmsmch.deckbuilder.data.rotation.RotationStore
import com.lvsmsmch.deckbuilder.data.update.UpdateNotifier
import com.lvsmsmch.deckbuilder.data.update.UpdateRunner
import com.lvsmsmch.deckbuilder.domain.repositories.CardRepository
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository
import com.lvsmsmch.deckbuilder.domain.repositories.PreferencesRepository
import com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository
import com.lvsmsmch.deckbuilder.domain.repositories.SavedDeckRepository
import com.lvsmsmch.deckbuilder.presentation.PendingDeckAdditions
import com.lvsmsmch.deckbuilder.presentation.SnackbarController
import com.lvsmsmch.deckbuilder.presentation.platform.AppInfo
import com.lvsmsmch.deckbuilder.presentation.platform.NetworkMonitor
import com.lvsmsmch.deckbuilder.presentation.platform.NetworkType
import com.lvsmsmch.deckbuilder.util.AppLog
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val HSJSON = named("hsjson")
val HSJSON_BUILD = named("hsjson_build")

/**
 * Platform-neutral data wiring. The platform module must additionally provide:
 * DataStore<Preferences>, AppDatabase, Toaster, NetworkMonitor, AppInfo,
 * CrashLogger. Ktor picks the engine from the classpath (OkHttp on Android,
 * Darwin on iOS).
 */
val commonDataModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
        }
    }

    // HearthstoneJSON CDN client — no auth, follows redirects normally. The
    // long request timeout covers the ~25 MB cards.json download.
    single<HttpClient>(HSJSON) {
        val debug = get<AppInfo>().isDebug
        HttpClient {
            install(ContentNegotiation) { json(get<Json>()) }
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 180_000
                socketTimeoutMillis = 90_000
            }
            if (debug) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            AppLog.d("DB.Http", message)
                        }
                    }
                    level = LogLevel.INFO
                }
            }
        }
    }

    // Client for resolving build numbers from the `latest` index — short
    // timeouts, no redirect following.
    single<HttpClient>(HSJSON_BUILD) {
        HttpClient {
            followRedirects = false
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                requestTimeoutMillis = 15_000
            }
        }
    }

    single { SessionLog() }
    single { SnackbarController() }
    single { PendingDeckAdditions() }
    single { UpdateNotifier() }

    single { HsJsonApi(client = get(HSJSON), notifier = get()) }
    single { BuildChecker(client = get(HSJSON_BUILD)) }
    single { HsJsonBuildStore(store = get()) }
    single {
        HsJsonRepository(
            api = get(),
            buildChecker = get(),
            dao = get(),
            builds = get(),
            json = get(),
            sessionLog = get(),
            notifier = get(),
        )
    }

    single { RotationApi(client = get(HSJSON), json = get()) }
    single { RotationStore(store = get()) }
    single<RotationRepository> { RotationRepositoryImpl(api = get(), store = get()) }

    single { get<AppDatabase>().savedDeckDao() }
    single { get<AppDatabase>().hsJsonCardDao() }

    single<PreferencesRepository> { PreferencesRepositoryImpl(store = get()) }
    single { CurrentLocaleProvider(prefs = get()) }

    single<CardRepository> {
        CardRepositoryImpl(
            hsJson = get(),
            dao = get(),
            locales = get(),
            sessionLog = get(),
            rotation = get(),
        )
    }
    single<DeckRepository> { DeckRepositoryImpl(hsJson = get(), locales = get(), sessionLog = get()) }
    single<SavedDeckRepository> {
        val hsJson = get<HsJsonRepository>()
        val locales = get<CurrentLocaleProvider>()
        SavedDeckRepositoryImpl(
            dao = get(),
            manaCostsOf = { ids -> hsJson.manaCostsByDbfIds(locales.resolve(), ids) },
        )
    }

    single {
        UpdateRunner(
            prefs = get(),
            hsJson = get(),
            rotation = get(),
            notifier = get(),
            crash = get(),
            isMeteredNetwork = { get<NetworkMonitor>().currentNetworkType() == NetworkType.Mobile },
        )
    }
}
