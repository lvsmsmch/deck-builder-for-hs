package com.lvsmsmch.deckbuilder.data.hsjson

import com.lvsmsmch.deckbuilder.util.AppLog
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

private const val TAG = "DB.HsJson.Build"

/**
 * Resolves the latest HearthstoneJSON build by parsing the directory index at
 * `/v1/latest/`. The historical HEAD-redirect trick on
 * `/v1/latest/{locale}/cards.collectible.json` no longer works — Cloudflare
 * serves the JSON directly with HTTP 200, so `Location` is never set and the
 * final URL still contains the literal segment `latest`.
 *
 * The index is a tree-style HTML listing; its first `/v1/{digits}` link points
 * to the current build directory, which is the same for every locale. The
 * `hsJsonLocale` parameter is accepted for API compatibility but unused.
 */
class BuildChecker(
    private val client: HttpClient,
    private val baseUrl: String = "https://api.hearthstonejson.com/v1/",
) {
    suspend fun latestBuild(@Suppress("UNUSED_PARAMETER") hsJsonLocale: String): String? =
        runCatching {
            val resp = client.get("${baseUrl}latest/")
            if (!resp.status.isSuccess()) error("HTTP ${resp.status.value}")
            parseBuildFromIndex(resp.bodyAsText())
        }.onFailure { AppLog.w(TAG, "latestBuild failed: ${it.message}") }
            .getOrNull()

    internal fun parseBuildFromIndex(html: String): String? =
        BUILD_LINK_REGEX.find(html)?.groupValues?.get(1)

    companion object {
        private val BUILD_LINK_REGEX = Regex("""href="/v1/(\d+)/?"""")
    }
}
