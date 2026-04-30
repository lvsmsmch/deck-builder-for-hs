package com.lvsmsmch.deckbuilder.data.repository

import android.util.Log
import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity
import com.lvsmsmch.deckbuilder.data.hsjson.HsJsonRepository
import com.lvsmsmch.deckbuilder.data.hsjson.parseClassTokens
import com.lvsmsmch.deckbuilder.data.hsjson.toDomain
import com.lvsmsmch.deckbuilder.data.hsjson.toDomainSlug
import com.lvsmsmch.deckbuilder.data.prefs.CurrentLocaleProvider
import com.lvsmsmch.deckbuilder.domain.common.Result
import com.lvsmsmch.deckbuilder.domain.common.runCatchingResult
import com.lvsmsmch.deckbuilder.domain.entities.Card
import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.Page
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey
import com.lvsmsmch.deckbuilder.domain.repositories.CardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DB.CardRepo"

class CardRepositoryImpl(
    private val hsJson: HsJsonRepository,
    private val locales: CurrentLocaleProvider,
) : CardRepository {

    override suspend fun getCard(idOrSlug: String, locale: String?): Result<Card> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val resolved = locales.resolve(locale)
                val snap = hsJson.ensureLoaded(resolved)
                val asInt = idOrSlug.toIntOrNull()
                val row = if (asInt != null) {
                    snap.cards.firstOrNull { it.dbfId == asInt }
                } else {
                    snap.cards.firstOrNull { it.cardId.equals(idOrSlug, ignoreCase = true) }
                } ?: error("Card not found in HsJson pool: $idOrSlug")
                row.toDomain()
            }.also { r ->
                when (r) {
                    is Result.Success -> Log.i(TAG, "getCard: OK idOrSlug=$idOrSlug name='${r.data.name}'")
                    is Result.Error -> Log.w(TAG, "getCard: FAILED idOrSlug=$idOrSlug: ${r.throwable.message}", r.throwable)
                }
            }
        }

    override suspend fun searchCards(
        filters: CardFilters,
        page: Int,
        pageSize: Int,
        locale: String?,
    ): Result<Page<Card>> = withContext(Dispatchers.IO) {
        runCatchingResult {
            val resolved = locales.resolve(locale)
            val snap = hsJson.ensureLoaded(resolved)
            val pred = buildPredicate(filters)
            val matched = snap.cards.filter(pred)
            val deduped = dedupeReprints(matched)
            val sorted = sort(deduped, filters.sort.key, filters.sort.direction)
            val total = sorted.size
            val pageCount = if (pageSize > 0 && total > 0) (total + pageSize - 1) / pageSize else 1
            val from = ((page - 1).coerceAtLeast(0)) * pageSize
            val items = sorted.drop(from).take(pageSize).map { it.toDomain() }
            Page(items = items, pageNumber = page, pageCount = pageCount, totalCount = total)
        }.also { r ->
            val summary = "page=$page " +
                "classes=${filters.classes} sets=${filters.sets.size} " +
                "rarities=${filters.rarities} mana=${filters.manaCosts} " +
                "q='${filters.textQuery}'"
            when (r) {
                is Result.Success -> Log.i(
                    TAG,
                    "searchCards: OK $summary → ${r.data.items.size}/${r.data.totalCount} items, " +
                        "pageCount=${r.data.pageCount}",
                )
                is Result.Error -> Log.w(TAG, "searchCards: FAILED $summary: ${r.throwable.message}", r.throwable)
            }
        }
    }

    private fun buildPredicate(filters: CardFilters): (HsJsonCardEntity) -> Boolean {
        // UI passes Blizzard-style lowercase slugs ("mage", "demonhunter"); HsJson
        // stores uppercase tokens ("MAGE", "DEMONHUNTER"). Compare on the lowercase
        // domain projection so both sides agree.
        val classes = filters.classes.map { it.lowercase() }.toSet()
        val sets = filters.sets.map { it.lowercase() }.toSet()
        val rarities = filters.rarities.map { it.lowercase() }.toSet()
        val types = filters.types.map { it.lowercase() }.toSet()
        val minionTypes = filters.minionTypes.map { it.lowercase() }.toSet()
        val spellSchools = filters.spellSchools.map { it.lowercase() }.toSet()
        val keywords = filters.keywords.map { it.lowercase() }.toSet()
        val expandedManaCosts: Set<Int> = filters.manaCosts
            .flatMap { c -> if (c >= 7) (7..30).toList() else listOf(c) }
            .toSortedSet()
        val q = filters.textQuery.trim().takeIf { it.isNotBlank() }?.lowercase()

        return predicate@{ row ->
            if (filters.collectibleOnly && !row.collectible) return@predicate false

            if (classes.isNotEmpty()) {
                val rowClasses = row.parseClassTokens().map { it.toDomainSlug() }
                if (rowClasses.none { it in classes }) return@predicate false
            }
            if (sets.isNotEmpty()) {
                val s = row.cardSet?.toDomainSlug() ?: return@predicate false
                if (s !in sets) return@predicate false
            }
            if (rarities.isNotEmpty()) {
                val r = row.rarity?.toDomainSlug() ?: return@predicate false
                if (r !in rarities) return@predicate false
            }
            if (types.isNotEmpty()) {
                val t = row.type?.toDomainSlug() ?: return@predicate false
                if (t !in types) return@predicate false
            }
            if (minionTypes.isNotEmpty()) {
                val races = (row.raceCsv?.trim(',')?.split(',') ?: emptyList())
                    .map { it.toDomainSlug() }
                if (races.none { it in minionTypes }) return@predicate false
            }
            if (spellSchools.isNotEmpty()) {
                val s = row.spellSchool?.toDomainSlug() ?: return@predicate false
                if (s !in spellSchools) return@predicate false
            }
            if (keywords.isNotEmpty()) {
                val mech = (row.mechanicsCsv?.trim(',')?.split(',') ?: emptyList())
                    .map { it.toDomainSlug() }
                if (mech.none { it in keywords }) return@predicate false
            }
            if (expandedManaCosts.isNotEmpty()) {
                val c = row.cost ?: return@predicate false
                if (c !in expandedManaCosts) return@predicate false
            }
            if (q != null) {
                val haystack = row.name.lowercase() + " " + (row.text?.lowercase().orEmpty())
                if (!haystack.contains(q)) return@predicate false
            }
            true
        }
    }

    /**
     * Hearthstone reprints the same card across multiple HsJson sets — most
     * commonly CORE (current rotation), LEGACY (Wild-only legacy pool), and
     * VANILLA (the Classic-mode parallel reprint pool). They share name, cost,
     * stats, and rules text, only the set token differs. The library should
     * surface one tile per canonical card.
     *
     * Strategy: drop VANILLA (discontinued mode, never desired in deck-builder
     * UI), then collapse remaining duplicates preferring CORE > anything else.
     * Within a tie we keep the lowest dbfId for stability across builds.
     */
    private fun dedupeReprints(rows: List<HsJsonCardEntity>): List<HsJsonCardEntity> {
        val noVanilla = rows.filter { !it.cardSet.equals("VANILLA", ignoreCase = true) }
        return noVanilla
            .groupBy { reprintKey(it) }
            .values
            .map { group ->
                if (group.size == 1) return@map group.first()
                group.minWithOrNull(reprintPreference) ?: group.first()
            }
    }

    private fun reprintKey(row: HsJsonCardEntity): String =
        listOf(
            row.name.lowercase(),
            row.cardClass.orEmpty(),
            row.cost?.toString() ?: "_",
            row.attack?.toString() ?: "_",
            row.health?.toString() ?: "_",
            row.type.orEmpty(),
            row.text?.trim()?.lowercase().orEmpty(),
        ).joinToString("|")

    private val reprintPreference: Comparator<HsJsonCardEntity> =
        compareBy<HsJsonCardEntity>(
            { if (it.cardSet.equals("CORE", ignoreCase = true)) 0 else 1 },
            { it.dbfId },
        )

    private fun sort(rows: List<HsJsonCardEntity>, key: SortKey, dir: SortDir): List<HsJsonCardEntity> {
        // DATE_ADDED uses dbfId as a proxy: higher dbfId == newer, so DESC = Newest, ASC = Oldest.
        // Other keys are flipped via Comparator.reversed() when dir == DESC.
        val base: Comparator<HsJsonCardEntity> = when (key) {
            SortKey.MANA_COST -> compareBy({ it.cost ?: Int.MAX_VALUE }, { it.name })
            SortKey.NAME -> compareBy { it.name }
            SortKey.DATE_ADDED -> compareByDescending<HsJsonCardEntity> { it.dbfId }.thenBy { it.name }
            SortKey.GROUP_BY_CLASS -> compareBy({ it.cardClass ?: "" }, { it.cost ?: Int.MAX_VALUE }, { it.name })
        }
        val cmp = if (dir == SortDir.DESC) base.reversed() else base
        return rows.sortedWith(cmp)
    }
}
