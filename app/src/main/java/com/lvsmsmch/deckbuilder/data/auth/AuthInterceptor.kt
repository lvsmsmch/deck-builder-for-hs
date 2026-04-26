package com.lvsmsmch.deckbuilder.data.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Injects the bearer token. On 401/403, refreshes the token once and
 * retries the request. The OAuth endpoint itself is excluded — a request
 * to oauth.battle.net carries Basic auth, not Bearer.
 */
class AuthInterceptor(
    private val tokens: TokenCache,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.url.host.endsWith("oauth.battle.net")) return chain.proceed(original)

        val token = runBlocking { tokens.current() }
        val authed = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        val resp = chain.proceed(authed)
        if (resp.code != 401 && resp.code != 403) return resp

        resp.close()
        val fresh = runBlocking { tokens.refresh(stale = token) }
        val retry = original.newBuilder()
            .header("Authorization", "Bearer $fresh")
            .build()
        return chain.proceed(retry)
    }
}
