package com.lvsmsmch.deckbuilder.data.repository

import android.util.Log
import com.lvsmsmch.deckbuilder.data.db.dao.MetadataDao
import com.lvsmsmch.deckbuilder.data.db.entity.MetadataBlobEntity
import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.network.dto.MetadataAllDto
import com.lvsmsmch.deckbuilder.data.network.mapper.toDomain
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.runCatchingResult
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val TAG = "DB.MetadataRepo"

class MetadataRepositoryImpl(
    private val api: HearthstoneApi,
    private val dao: MetadataDao,
    private val json: Json,
    private val locales: CurrentLocaleProvider,
    private val freshnessWindowMs: Long = DEFAULT_FRESHNESS_WINDOW_MS,
    private val nowMs: () -> Long = System::currentTimeMillis,
) : MetadataRepository {

    private val _current = MutableStateFlow<Metadata?>(null)
    override val current: StateFlow<Metadata?> = _current.asStateFlow()

    private val refreshGate = Mutex()

    override suspend fun loadFromCache(locale: String?): Metadata? = withContext(Dispatchers.IO) {
        val resolved = locales.resolve(locale)
        val row = dao.getBlob(resolved)
        if (row == null) {
            Log.i(TAG, "loadFromCache: miss for locale=$resolved")
            return@withContext null
        }
        val dto = runCatching { json.decodeFromString<MetadataAllDto>(row.payloadJson) }
            .onFailure { Log.w(TAG, "loadFromCache: payload decode failed for locale=$resolved", it) }
            .getOrNull() ?: return@withContext null
        val ageMs = nowMs() - row.refreshedAtMs
        val meta = dto.toDomain(locale = row.locale, refreshedAtMs = row.refreshedAtMs)
            .also { _current.value = it }
        Log.i(
            TAG,
            "loadFromCache: hit locale=$resolved ageMs=$ageMs " +
                "(${meta.classes.size} classes, ${meta.sets.size} sets, ${meta.rarities.size} rarities)",
        )
        meta
    }

    override suspend fun refresh(locale: String?, force: Boolean): Result<Metadata> = withContext(Dispatchers.IO) {
        val resolved = locales.resolve(locale)
        refreshGate.withLock {
            val cached = _current.value
            if (!force && cached != null && cached.locale == resolved && isFresh(cached.refreshedAtMs)) {
                Log.i(TAG, "refresh: skip — cached fresh for locale=$resolved (age=${nowMs() - cached.refreshedAtMs}ms)")
                return@withLock Result.Success(cached)
            }
            Log.i(TAG, "refresh: starting locale=$resolved force=$force")
            runCatchingResult {
                val dto = api.metadata(locale = resolved)
                val now = nowMs()
                val payloadJson = json.encodeToString(MetadataAllDto.serializer(), dto)
                dao.upsert(MetadataBlobEntity(locale = resolved, payloadJson = payloadJson, refreshedAtMs = now))
                dto.toDomain(locale = resolved, refreshedAtMs = now).also { _current.value = it }
            }.also { r ->
                when (r) {
                    is Result.Success -> Log.i(
                        TAG,
                        "refresh: OK locale=$resolved " +
                            "(${r.data.classes.size} classes, ${r.data.sets.size} sets, ${r.data.rarities.size} rarities)",
                    )
                    is Result.Error -> Log.w(TAG, "refresh: FAILED locale=$resolved: ${r.throwable.message}", r.throwable)
                }
            }
        }
    }

    private fun isFresh(refreshedAtMs: Long): Boolean =
        nowMs() - refreshedAtMs < freshnessWindowMs

    companion object {
        // Plan §10.11 — refresh per week is plenty.
        val DEFAULT_FRESHNESS_WINDOW_MS: Long = 7L * 24 * 60 * 60 * 1000
    }
}
