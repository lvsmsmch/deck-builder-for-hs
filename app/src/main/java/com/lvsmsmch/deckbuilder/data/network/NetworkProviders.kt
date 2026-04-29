package com.lvsmsmch.deckbuilder.data.network

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.lvsmsmch.deckbuilder.BuildConfig
import com.lvsmsmch.deckbuilder.data.auth.AuthInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkProviders {

    private const val OAUTH_CLIENT_KEY = "oauth"
    private const val API_CLIENT_KEY = "api"

    const val HSJSON_BASE_URL = "https://api.hearthstonejson.com/"

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val converter = json.asConverterFactory("application/json".toMediaType())

    /** Plain client for the OAuth endpoint — no AuthInterceptor (would cause a token-refresh recursion). */
    fun oAuthClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor()) }
            .build()

    /** Authenticated client for `api.blizzard.com`. */
    fun apiClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor()) }
            .build()

    fun oAuthRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BLIZZARD_OAUTH_URL)
            .client(client)
            .addConverterFactory(converter)
            .build()

    /** Plain client for HearthstoneJSON CDN — no auth, follows redirects normally. */
    fun hsJsonClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor()) }
            .build()

    /** HEAD-only client for resolving build numbers from the `latest` redirect. */
    fun hsJsonBuildClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()

    fun hsJsonRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(HSJSON_BASE_URL)
            .client(client)
            .addConverterFactory(converter)
            .build()

    fun apiRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BLIZZARD_API_URL)
            .client(client)
            .addConverterFactory(converter)
            .build()

    private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
}
