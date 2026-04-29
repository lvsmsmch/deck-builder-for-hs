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
import com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val HSJSON_BASE_URL = "https://api.hearthstonejson.com/"

private val HSJSON = named("hsjson")
private val HSJSON_BUILD = named("hsjson_build")

private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
}

val networkModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
        }
    }

    // HearthstoneJSON CDN client — no auth, follows redirects normally.
    single<OkHttpClient>(HSJSON) {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor()) }
            .build()
    }

    // HEAD-only client for resolving build numbers from the `latest` redirect.
    single<OkHttpClient>(HSJSON_BUILD) {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

    single<Retrofit>(HSJSON) {
        val converter = get<Json>().asConverterFactory("application/json".toMediaType())
        Retrofit.Builder()
            .baseUrl(HSJSON_BASE_URL)
            .client(get(HSJSON))
            .addConverterFactory(converter)
            .build()
    }

    single<HsJsonApi> { get<Retrofit>(HSJSON).create(HsJsonApi::class.java) }
    single { BuildChecker(client = get(HSJSON_BUILD)) }
    single { HsJsonBuildStore(store = get()) }
    single {
        HsJsonRepository(
            api = get(),
            buildChecker = get(),
            dao = get(),
            builds = get(),
            json = get(),
        )
    }

    // Rotation pipeline (raw GitHub) — re-uses the HsJson client, no auth needed.
    single { RotationApi(client = get(HSJSON), json = get()) }
    single { RotationStore(store = get()) }
    single<RotationRepository> { RotationRepositoryImpl(api = get(), store = get()) }

    // Persistence
    single { AppDatabase.build(androidContext()) }
    single { get<AppDatabase>().savedDeckDao() }
    single { get<AppDatabase>().hsJsonCardDao() }

    // DataStore for prefs
    single { androidContext().userPrefsStore }

    // Background update plumbing.
    single { UpdateNotifier() }
    single {
        UpdateRunner(
            prefs = get(),
            hsJson = get(),
            rotation = get(),
            notifier = get(),
            crash = get(),
        )
    }
}
