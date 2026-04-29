package com.lvsmsmch.deckbuilder.di

import com.lvsmsmch.deckbuilder.BuildConfig
import com.lvsmsmch.deckbuilder.data.auth.AuthInterceptor
import com.lvsmsmch.deckbuilder.data.auth.OAuthApi
import com.lvsmsmch.deckbuilder.data.auth.TokenCache
import com.lvsmsmch.deckbuilder.data.db.AppDatabase
import com.lvsmsmch.deckbuilder.data.hsjson.BuildChecker
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonApi
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonBuildStore
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonRepository
import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.network.NetworkProviders
import com.lvsmsmch.deckbuilder.data.prefs.userPrefsStore
import com.lvsmsmch.deckbuilder.data.rotation.RotationApi
import com.lvsmsmch.deckbuilder.data.rotation.RotationRepositoryImpl
import com.lvsmsmch.deckbuilder.data.rotation.RotationStore
import com.lvsmsmch.deckbuilder.domain.repositories.RotationRepository
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

private val OAUTH = named("oauth")
private val API = named("api")
private val HSJSON = named("hsjson")
private val HSJSON_BUILD = named("hsjson_build")

val networkModule = module {

    // OAuth pipeline (no AuthInterceptor — we'd recurse).
    single<OkHttpClient>(OAUTH) { NetworkProviders.oAuthClient() }
    single<Retrofit>(OAUTH) { NetworkProviders.oAuthRetrofit(get(OAUTH)) }
    single<OAuthApi> { get<Retrofit>(OAUTH).create(OAuthApi::class.java) }

    single {
        TokenCache(
            oAuthApi = get(),
            clientId = BuildConfig.BLIZZARD_CLIENT_ID,
            clientSecret = BuildConfig.BLIZZARD_CLIENT_SECRET,
        )
    }

    single { AuthInterceptor(tokens = get()) }

    // Authenticated API pipeline
    single<OkHttpClient>(API) { NetworkProviders.apiClient(authInterceptor = get()) }
    single<Retrofit>(API) { NetworkProviders.apiRetrofit(get(API)) }
    single<HearthstoneApi> { get<Retrofit>(API).create(HearthstoneApi::class.java) }

    single { NetworkProviders.json }

    // HearthstoneJSON pipeline (no auth).
    single<OkHttpClient>(HSJSON) { NetworkProviders.hsJsonClient() }
    single<OkHttpClient>(HSJSON_BUILD) { NetworkProviders.hsJsonBuildClient() }
    single<Retrofit>(HSJSON) { NetworkProviders.hsJsonRetrofit(get(HSJSON)) }
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
    single { get<AppDatabase>().metadataDao() }
    single { get<AppDatabase>().savedDeckDao() }
    single { get<AppDatabase>().hsJsonCardDao() }

    // DataStore for prefs
    single { androidContext().userPrefsStore }
}
