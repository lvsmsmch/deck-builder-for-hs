package com.lvsmsmch.deckbuilder.data.repository

import android.util.Log
import com.lvsmsmch.deckbuilder.data.network.HearthstoneApi
import com.lvsmsmch.deckbuilder.data.network.mapper.toDomain
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.runCatchingResult
import com.lvsmsmch.deckbuilder.domain.entities.CardBack
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.domain.repositories.CardBackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DB.CardBackRepo"

class CardBackRepositoryImpl(
    private val api: HearthstoneApi,
    private val locales: CurrentLocaleProvider,
) : CardBackRepository {

    override suspend fun search(
        category: String?,
        textQuery: String,
        page: Int,
        pageSize: Int,
        locale: String?,
    ): Result<Page<CardBack>> = withContext(Dispatchers.IO) {
        runCatchingResult {
            val resolved = locales.resolve(locale)
            val params = buildMap {
                put("locale", resolved)
                put("page", page.toString())
                put("pageSize", pageSize.toString())
                put("sort", "dateAdded:desc")
                if (!category.isNullOrBlank()) put("cardBackCategory", category)
                if (textQuery.isNotBlank()) put("textFilter", textQuery.trim())
            }
            Log.d(TAG, "search: → API params=$params")
            val resp = api.cardBacks(params)
            Page(
                items = resp.cardBacks.map { it.toDomain() },
                pageNumber = resp.page,
                pageCount = resp.pageCount,
                totalCount = resp.cardCount,
            )
        }.also { r ->
            val summary = "page=$page category=$category q='$textQuery'"
            when (r) {
                is Result.Success -> Log.i(
                    TAG, "search: OK $summary → ${r.data.items.size}/${r.data.totalCount} items",
                )
                is Result.Error -> Log.w(TAG, "search: FAILED $summary: ${r.throwable.message}", r.throwable)
            }
        }
    }
}
