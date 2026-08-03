package com.lvsmsmch.deckbuilder.data.db

import com.lvsmsmch.deckbuilder.domain.entities.CardFilters
import com.lvsmsmch.deckbuilder.domain.entities.CardFormatFilter
import com.lvsmsmch.deckbuilder.domain.entities.SortDir
import com.lvsmsmch.deckbuilder.domain.entities.SortKey

/**
 * Translates [CardFilters] into SQL so filtering, sorting and paging happen in
 * SQLite instead of over a fully materialized card list in memory.
 *
 * Filter values are domain slugs (`the-barrens`, `demonhunter`); the table
 * stores HearthstoneJSON tokens (`THE_BARRENS`, `DEMONHUNTER`), so every value
 * is converted with [toDbToken] before binding.
 */
internal object CardQuery {

    data class Sql(val where: String, val args: List<Any>, val orderBy: String)

    fun build(filters: CardFilters, locale: String, standardSets: Set<String>): Sql {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<Any>()

        clauses += "locale = ?"
        args.add(locale)

        clauses += "(cardSet IS NULL OR (cardSet NOT IN (${placeholders(HiddenCardSets.TOKENS.size)}) " +
            "AND cardSet NOT LIKE '${HiddenCardSets.PLACEHOLDER_PREFIX}%'))"
        args.addAll(HiddenCardSets.TOKENS)

        if (filters.collectibleOnly) {
            clauses += "collectible = 1"
            // Cosmetic hero skins are collectible but never playable cards.
            clauses += "NOT (type = 'HERO' AND (text IS NULL OR text = ''))"
        }

        if (filters.format == CardFormatFilter.STANDARD) {
            if (standardSets.isEmpty()) {
                clauses += "0"
            } else {
                val tokens = standardSets.map { it.toDbToken() }
                clauses += "cardSet IN (${placeholders(tokens.size)})"
                args.addAll(tokens)
            }
        }

        // A card belongs to a class either through its multi-class list or its
        // single cardClass column.
        filters.classes.takeIf { it.isNotEmpty() }?.let { classes ->
            val tokens = classes.map { it.toDbToken() }
            val ors = tokens.joinToString(" OR ") { "(classesCsv LIKE ? OR cardClass = ?)" }
            clauses += "($ors)"
            tokens.forEach { args.add("%,$it,%"); args.add(it) }
        }

        filters.sets.inClause("cardSet", clauses, args)
        filters.rarities.inClause("rarity", clauses, args)
        filters.types.inClause("type", clauses, args)
        filters.spellSchools.inClause("spellSchool", clauses, args)
        filters.minionTypes.likeCsvClause("raceCsv", clauses, args)
        filters.keywords.likeCsvClause("mechanicsCsv", clauses, args)

        filters.manaCosts.takeIf { it.isNotEmpty() }?.let { costs ->
            val exact = costs.filter { it < MAX_EXACT_COST }
            val parts = mutableListOf<String>()
            if (exact.isNotEmpty()) {
                parts += "cost IN (${placeholders(exact.size)})"
                args.addAll(exact)
            }
            // The last chip means "7 and above".
            if (costs.any { it >= MAX_EXACT_COST }) {
                parts += "cost >= ?"
                args.add(MAX_EXACT_COST)
            }
            clauses += "(${parts.joinToString(" OR ")})"
        }

        filters.textQuery.trim().takeIf { it.isNotBlank() }?.let { query ->
            clauses += "searchText LIKE ?"
            args.add("%${query.lowercase()}%")
        }

        return Sql(
            where = clauses.joinToString(" AND "),
            args = args,
            orderBy = orderBy(filters.sort.key, filters.sort.direction),
        )
    }

    private fun orderBy(key: SortKey, dir: SortDir): String {
        val desc = dir == SortDir.DESC
        fun dir(ascending: Boolean) = if (ascending != desc) "ASC" else "DESC"
        // Cards without a cost (heroes) sort last, as they did in memory.
        val costExpr = "COALESCE(cost, 2147483647)"
        return when (key) {
            SortKey.MANA_COST -> "$costExpr ${dir(true)}, name ${dir(true)}"
            SortKey.NAME -> "name ${dir(true)}"
            // Higher dbfId means newer, so "newest first" is dbfId DESC.
            SortKey.DATE_ADDED -> "dbfId ${dir(false)}, name ${dir(true)}"
            SortKey.GROUP_BY_CLASS -> "COALESCE(cardClass, '') ${dir(true)}, $costExpr ${dir(true)}, name ${dir(true)}"
        }
    }

    private fun Set<String>.inClause(column: String, clauses: MutableList<String>, args: MutableList<Any>) {
        if (isEmpty()) return
        val tokens = map { it.toDbToken() }
        clauses += "$column IN (${placeholders(tokens.size)})"
        args.addAll(tokens)
    }

    private fun Set<String>.likeCsvClause(column: String, clauses: MutableList<String>, args: MutableList<Any>) {
        if (isEmpty()) return
        val tokens = map { it.toDbToken() }
        clauses += "(" + tokens.joinToString(" OR ") { "$column LIKE ?" } + ")"
        tokens.forEach { args.add("%,$it,%") }
    }

    private fun placeholders(count: Int): String = List(count) { "?" }.joinToString(", ")

    /** `the-barrens` -> `THE_BARRENS`. */
    private fun String.toDbToken(): String = uppercase().replace('-', '_')

    private const val MAX_EXACT_COST = 7
}
