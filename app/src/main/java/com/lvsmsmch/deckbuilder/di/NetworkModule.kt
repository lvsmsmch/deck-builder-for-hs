package com.lvsmsmch.deckbuilder.di

import com.lvsmsmch.deckbuilder.BuildConfig
import com.lvsmsmch.deckbuilder.data.auth.AuthInterceptor
import com.lvsmsmch.deckbuilder.data.auth.OAuthApi
import com.lvsmsmch.deckbuilder.data.auth.TokenCache
import com.lvsmsmch.deckbuilder.data.db.AppDatabase
import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.network.NetworkProviders
import com.lvsmsmch.deckbuilder.data.prefs.userPrefsStore
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

private val OAUTH = named("oauth")
private val API = named("api")

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

    // Persistence
    single { AppDatabase.build(androidContext()) }
    single { get<AppDatabase>().metadataDao() }
    single { get<AppDatabase>().savedDeckDao() }

    // DataStore for prefs
    single { androidContext().userPrefsStore }
}
