package com.lvsmsmch.deckbuilder.data.repository

import android.util.Log
import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.network.mapper.toDomain
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.runCatchingResult
import com.lvsmsmch.deckbuilder.domain.entities.Deck
import com.lvsmsmch.deckbuilder.domain.entities.Metadata
import com.lvsmsmch.deckbuilder.domain.repositories.DeckRepository
import com.lvsmsmch.deckbuilder.domain.repositories.MetadataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DB.DeckRepo"

class DeckRepositoryImpl(
    private val api: HearthstoneApi,
    private val metadata: MetadataRepository,
    private val locales: CurrentLocaleProvider,
) : DeckRepository {

    override suspend fun decodeByCode(code: String, locale: String?): Result<Deck> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val resolved = locales.resolve(locale)
                val meta = currentMetadataOrLoad(resolved)
                api.deckByCode(locale = resolved, code = code).toDomain(meta)
            }.also { r ->
                val codePreview = code.take(12) + if (code.length > 12) "…" else ""
                when (r) {
                    is Result.Success -> Log.i(
                        TAG,
                        "decodeByCode: OK code=$codePreview cards=${r.data.cardCount} hero=${r.data.heroClass?.slug}",
                    )
                    is Result.Error -> Log.w(
                        TAG, "decodeByCode: FAILED code=$codePreview: ${r.throwable.message}", r.throwable,
                    )
                }
            }
        }

    override suspend fun assembleByIds(
        ids: List<Int>,
        heroCardId: Int?,
        locale: String?,
    ): Result<Deck> = withContext(Dispatchers.IO) {
        runCatchingResult {
            require(ids.isNotEmpty()) { "Deck must have at least one card" }
            val resolved = locales.resolve(locale)
            val meta = currentMetadataOrLoad(resolved)
            api.deckByIds(
                locale = resolved,
                ids = ids.joinToString(","),
                heroCardId = heroCardId,
            ).toDomain(meta)
        }.also { r ->
            when (r) {
                is Result.Success -> Log.i(
                    TAG,
                    "assembleByIds: OK ids=${ids.size} hero=$heroCardId → code='${r.data.code.take(12)}…' cards=${r.data.cardCount}",
                )
                is Result.Error -> Log.w(
                    TAG, "assembleByIds: FAILED ids=${ids.size} hero=$heroCardId: ${r.throwable.message}", r.throwable,
                )
            }
        }
    }

    private suspend fun currentMetadataOrLoad(locale: String): Metadata =
        metadata.current.value ?: metadata.loadFromCache(locale) ?: Metadata.Empty
}
