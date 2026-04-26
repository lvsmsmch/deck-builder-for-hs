package com.lvsmsmch.deckbuilder.data.auth

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

private const val TAG = "DB.AuthInt"

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

        val pathPreview = original.url.encodedPath
        val token = try {
            runBlocking { tokens.current() }
        } catch (t: Throwable) {
            Log.e(TAG, "intercept: token fetch FAILED for $pathPreview: ${t.message}", t)
            throw t
        }
        val authed = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        val resp = chain.proceed(authed)
        if (resp.code != 401 && resp.code != 403) {
            if (!resp.isSuccessful) Log.w(TAG, "intercept: $pathPreview → HTTP ${resp.code}")
            return resp
        }

        Log.w(TAG, "intercept: $pathPreview → HTTP ${resp.code}, refreshing token and retrying once")
        resp.close()
        val fresh = try {
            runBlocking { tokens.refresh(stale = token) }
        } catch (t: Throwable) {
            Log.e(TAG, "intercept: token REFRESH failed for $pathPreview: ${t.message}", t)
            throw t
        }
        val retry = original.newBuilder()
            .header("Authorization", "Bearer $fresh")
            .build()
        val retried = chain.proceed(retry)
        if (!retried.isSuccessful) Log.w(TAG, "intercept: $pathPreview retry → HTTP ${retried.code}")
        return retried
    }
}
