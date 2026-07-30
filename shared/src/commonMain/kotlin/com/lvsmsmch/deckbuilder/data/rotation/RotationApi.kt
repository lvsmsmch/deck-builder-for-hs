package com.lvsmsmch.deckbuilder.data.rotation

import com.lvsmsmch.deckbuilder.util.AppLog
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val TAG = "DB.Rotation.Api"

/**
 * Thin Ktor wrapper. Endpoints:
 *
 * - raw enums.py source from `raw.githubusercontent.com`
 * - raw utils/__init__.py source from `raw.githubusercontent.com`
 * - latest commit metadata for that file from the GitHub commits API
 */
class RotationApi(
    private val client: HttpClient,
    private val json: Json,
    private val rawUrl: String = DEFAULT_RAW_URL,
    private val utilsUrl: String = DEFAULT_UTILS_URL,
    private val commitsUrl: String = DEFAULT_COMMITS_URL,
) {

    data class CommitInfo(val sha: String, val committedAtIso: String?)

    suspend fun fetchEnumsSource(): String? = fetchText(rawUrl, "fetchEnumsSource")

    suspend fun fetchUtilsSource(): String? = fetchText(utilsUrl, "fetchUtilsSource")

    suspend fun fetchLatestCommit(): CommitInfo? = runCatching {
        val resp = client.get(commitsUrl) {
            header("Accept", "application/vnd.github+json")
        }
        if (!resp.status.isSuccess()) error("HTTP ${resp.status.value}")
        val arr = json.parseToJsonElement(resp.bodyAsText()).jsonArray
        val first = arr.firstOrNull()?.jsonObject ?: return@runCatching null
        val sha = first["sha"]?.jsonPrimitive?.content ?: return@runCatching null
        val date = first["commit"]?.jsonObject
            ?.get("committer")?.jsonObject
            ?.get("date")?.jsonPrimitive?.content
        CommitInfo(sha = sha, committedAtIso = date)
    }.onFailure { AppLog.w(TAG, "fetchLatestCommit failed: ${it.message}") }
        .getOrNull()

    private suspend fun fetchText(url: String, what: String): String? = runCatching {
        val resp = client.get(url)
        if (!resp.status.isSuccess()) error("HTTP ${resp.status.value}")
        resp.bodyAsText()
    }.onFailure { AppLog.w(TAG, "$what failed: ${it.message}") }
        .getOrNull()

    companion object {
        const val DEFAULT_RAW_URL =
            "https://raw.githubusercontent.com/HearthSim/python-hearthstone/master/hearthstone/enums.py"
        const val DEFAULT_UTILS_URL =
            "https://raw.githubusercontent.com/HearthSim/python-hearthstone/master/hearthstone/utils/__init__.py"
        const val DEFAULT_COMMITS_URL =
            "https://api.github.com/repos/HearthSim/python-hearthstone/commits?path=hearthstone/enums.py&per_page=1"
    }
}
