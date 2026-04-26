package com.lvsmsmch.deckbuilder.data.auth

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "DB.TokenCache"

/**
 * Holds the latest OAuth bearer token plus an expiry timestamp.
 * Concurrent callers waiting for a refresh are gated by a [Mutex] so
 * a thundering herd after expiry triggers exactly one network call.
 */
class TokenCache(
    private val oAuthApi: OAuthApi,
    private val clientId: String,
    private val clientSecret: String,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    private val mutex = Mutex()

    @Volatile private var token: String? = null
    @Volatile private var expiresAtMs: Long = 0L

    /** Refreshes ~1 minute before the server-reported expiry to avoid edge races. */
    private val safetyMarginMs = 60_000L

    suspend fun current(): String {
        token?.takeIf { nowMs() < expiresAtMs - safetyMarginMs }?.let { return it }
        return refreshLocked(stale = null)
    }

    /**
     * Forces a refresh. Pass the stale token to avoid wasted re-fetches when
     * multiple coroutines all see the same 401 at once — only the first one
     * actually hits the network.
     */
    suspend fun refresh(stale: String? = null): String = refreshLocked(stale)

    private suspend fun refreshLocked(stale: String?): String = mutex.withLock {
        val cur = token
        if (cur != null && cur != stale && nowMs() < expiresAtMs - safetyMarginMs) {
            return@withLock cur
        }
        if (clientId.isBlank() || clientSecret.isBlank()) {
            Log.e(TAG, "refresh: BLIZZARD_CLIENT_ID or _SECRET is blank — set them in local.properties or env")
        }
        val basic = "Basic " + Base64.encodeToString(
            "$clientId:$clientSecret".toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP,
        )
        try {
            val resp = oAuthApi.token(basicAuth = basic)
            token = resp.accessToken
            expiresAtMs = nowMs() + resp.expiresInSeconds * 1000L
            Log.i(TAG, "refresh: OK expiresInSec=${resp.expiresInSeconds} (clientId='${clientId.take(6)}…')")
            resp.accessToken
        } catch (t: Throwable) {
            Log.e(TAG, "refresh: FAILED (clientId='${clientId.take(6)}…' empty=${clientId.isBlank()}): ${t.message}", t)
            throw t
        }
    }
}
