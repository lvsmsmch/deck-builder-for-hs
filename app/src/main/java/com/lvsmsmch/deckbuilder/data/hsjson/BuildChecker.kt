package com.lvsmsmch.deckbuilder.data.hsjson

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request

private const val TAG = "DB.HsJson.Build"

/**
 * Resolves the latest HearthstoneJSON build for a locale by following the redirect
 * on `/v1/latest/{locale}/cards.collectible.json` and parsing the build segment
 * out of the final URL.
 *
 * We use a dedicated client with `followRedirects = false`; the redirect's
 * `Location` header carries the build number. If the server stops issuing a
 * redirect (returns 200 directly), we fall back to a HEAD on the same URL
 * after re-enabling redirects and inspect `request.url`.
 */
class BuildChecker(
    private val client: OkHttpClient,
    private val baseUrl: String = "https://api.hearthstonejson.com/v1/",
) {
    suspend fun latestBuild(hsJsonLocale: String): String? = runCatching {
        val url = "${baseUrl}latest/$hsJsonLocale/cards.collectible.json"
        val request = Request.Builder().url(url).head().build()
        client.newCall(request).execute().use { resp ->
            // followRedirects=false on the client → 30x with Location header.
            val location = resp.header("Location")
            if (resp.isRedirect && location != null) parseBuildFromUrl(location)
            else parseBuildFromUrl(resp.request.url.toString())
        }
    }.onFailure { Log.w(TAG, "latestBuild($hsJsonLocale) failed: ${it.message}") }
        .getOrNull()

    internal fun parseBuildFromUrl(url: String): String? {
        // .../v1/{build}/{locale}/cards.collectible.json
        val segments = url.substringAfter("://").split('/')
        val v1Idx = segments.indexOf("v1")
        if (v1Idx < 0 || v1Idx + 1 >= segments.size) return null
        val build = segments[v1Idx + 1]
        return build.takeIf { it.all(Char::isDigit) }
    }
}
