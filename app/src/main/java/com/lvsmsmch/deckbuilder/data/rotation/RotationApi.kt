package com.lvsmsmch.deckbuilder.data.rotation

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

private const val TAG = "DB.Rotation.Api"

/**
 * Thin OkHttp wrapper. Two endpoints:
 *
 * - raw enums.py source from `raw.githubusercontent.com`
 * - latest commit metadata for that file from the GitHub commits API
 */
class RotationApi(
    private val client: OkHttpClient,
    private val json: Json,
    private val rawUrl: String = DEFAULT_RAW_URL,
    private val commitsUrl: String = DEFAULT_COMMITS_URL,
) {

    data class CommitInfo(val sha: String, val committedAtIso: String?)

    suspend fun fetchEnumsSource(): String? = runCatching {
        val req = Request.Builder().url(rawUrl).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            resp.body?.string() ?: error("empty body")
        }
    }.onFailure { Log.w(TAG, "fetchEnumsSource failed: ${it.message}") }
        .getOrNull()

    suspend fun fetchLatestCommit(): CommitInfo? = runCatching {
        val req = Request.Builder().url(commitsUrl).get()
            .header("Accept", "application/vnd.github+json")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val body = resp.body?.string() ?: error("empty body")
            val arr = json.parseToJsonElement(body).jsonArray
            val first = arr.firstOrNull()?.jsonObject ?: return@use null
            val sha = first["sha"]?.jsonPrimitive?.content ?: return@use null
            val date = first["commit"]?.jsonObject
                ?.get("committer")?.jsonObject
                ?.get("date")?.jsonPrimitive?.content
            CommitInfo(sha = sha, committedAtIso = date)
        }
    }.onFailure { Log.w(TAG, "fetchLatestCommit failed: ${it.message}") }
        .getOrNull()

    companion object {
        const val DEFAULT_RAW_URL =
            "https://raw.githubusercontent.com/HearthSim/python-hearthstone/master/hearthstone/enums.py"
        const val DEFAULT_COMMITS_URL =
            "https://api.github.com/repos/HearthSim/python-hearthstone/commits?path=hearthstone/enums.py&per_page=1"
    }
}
