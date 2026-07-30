package com.lvsmsmch.deckbuilder.di

import com.lvsmsmch.deckbuilder.BuildConfig
import com.lvsmsmch.deckbuilder.data.db.AppDatabase
import com.lvsmsmch.deckbuilder.data.hsjson.BuildChecker
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonApi
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonBuildStore
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonRepository
import com.lvsmsmch.deckbuilder.data.prefs.userPrefsStore
import com.lvsmsmch.deckbuilder.data.rotation.RotationApi
import com.lvsmsmch.deckbuilder.data.rotation.RotationRepositoryImpl
import com.lvsmsmch.deckbuilder.data.rotation.RotationStore
import com.lvsmsmch.deckbuilder.data.update.UpdateNotifier
import com.lvsmsmch.deckbuilder.data.update.UpdateRunner
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val HSJSON = named("hsjson")
private val HSJSON_BUILD = named("hsjson_build")

val networkModule = module {

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
        HttpClient(OkHttp) {
            install(ContentNegotiation) { json(get<Json>()) }
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 180_000
                socketTimeoutMillis = 90_000
            }
            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            android.util.Log.d("DB.Http", message)
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
        HttpClient(OkHttp) {
            followRedirects = false
            install(HttpTimeout) {
                connectTimeoutMillis = 10_000
                requestTimeoutMillis = 15_000
            }
        }
    }

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

    // Rotation pipeline (raw GitHub) — re-uses the HsJson client, no auth needed.
    single { RotationApi(client = get(HSJSON), json = get()) }
    single { RotationStore(store = get()) }
    single<com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository> {
        RotationRepositoryImpl(api = get(), store = get())
    }

    // Persistence
    single { AppDatabase.build(androidContext()) }
    single { get<AppDatabase>().savedDeckDao() }
    single { get<AppDatabase>().hsJsonCardDao() }

    // DataStore for prefs
    single { androidContext().userPrefsStore }

    // Background update plumbing.
    single { UpdateNotifier() }
    single {
        val appContext = androidContext()
        UpdateRunner(
            prefs = get(),
            hsJson = get(),
            rotation = get(),
            notifier = get(),
            crash = get(),
            isMeteredNetwork = {
                val cm = appContext.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                    as? android.net.ConnectivityManager
                cm?.isActiveNetworkMetered == true
            },
        )
    }
}
