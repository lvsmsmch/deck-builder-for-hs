package com.lvsmsmch.deckbuilder.data.rotation

import java.time.LocalDate

/**
 * Parses `hearthstone/enums.py` from python-hearthstone:
 *
 * - [parseStandardSets] extracts the trailing `STANDARD_SETS = (CardSet.X, CardSet.Y, ...)`
 *   tuple, or the newer `STANDARD_SETS = { ZodiacYear.X: [CardSet.Y, ...] }` mapping.
 * - [parseCardSetEnum] extracts every member name of the `class CardSet(IntEnum)` block,
 *   used for the cross-check that detects when enums.py lags behind a new release.
 */
internal object EnumsParser {

    private val STANDARD_TUPLE_RE = Regex("""STANDARD_SETS\s*=\s*\(([^)]*)\)""", RegexOption.DOT_MATCHES_ALL)
    private val STANDARD_DICT_RE = Regex("""STANDARD_SETS\s*=\s*\{([\s\S]*?)^\}""", setOf(RegexOption.MULTILINE))
    private val STANDARD_DICT_ENTRY_RE = Regex(
        """ZodiacYear\.([A-Z][A-Z0-9_]*)\s*:\s*\[([\s\S]*?)\]""",
        RegexOption.MULTILINE,
    )
    private val ROTATION_DATES_RE = Regex("""ZODIAC_ROTATION_DATES\s*=\s*\{([\s\S]*?)^\}""", setOf(RegexOption.MULTILINE))
    private val ROTATION_DATE_ENTRY_RE = Regex(
        """ZodiacYear\.([A-Z][A-Z0-9_]*)\s*:\s*(?:datetime\((\d{4}),\s*(\d{1,2}),\s*(\d{1,2})\)|_EPOCH)""",
    )
    private val CARD_SET_TOKEN_RE = Regex("""CardSet\.([A-Z][A-Z0-9_]*)""")
    private val CARDSET_HEADER_RE = Regex("""class\s+CardSet\s*\([^)]+\)\s*:""")
    private val ENUM_MEMBER_RE = Regex("""^\s*([A-Z][A-Z0-9_]*)\s*=\s*-?\d+""")
    private val CLASS_HEADER_RE = Regex("""^\s*class\s+\w+""")

    fun parseStandardSets(source: String, today: LocalDate = LocalDate.now()): Set<String> {
        parseStandardTuple(source).takeIf { it.isNotEmpty() }?.let { return it }
        return parseStandardMapping(source, today)
    }

    private fun parseStandardTuple(source: String): Set<String> {
        val body = STANDARD_TUPLE_RE.find(source)?.groupValues?.get(1) ?: return emptySet()
        return parseCardSetTokens(body)
    }

    private fun parseStandardMapping(source: String, today: LocalDate): Set<String> {
        val standardBody = STANDARD_DICT_RE.find(source)?.groupValues?.get(1) ?: return emptySet()
        val byYear = STANDARD_DICT_ENTRY_RE.findAll(standardBody)
            .associate { match -> match.groupValues[1] to parseCardSetTokens(match.groupValues[2]) }
        if (byYear.isEmpty()) return emptySet()

        val currentYear = parseRotationDates(source, today)
            .filterKeys { it in byYear }
            .maxByOrNull { it.value }
            ?.key
            ?: byYear.keys.lastOrNull()

        return currentYear?.let { byYear[it] }.orEmpty()
    }

    private fun parseRotationDates(source: String, today: LocalDate): Map<String, LocalDate> {
        val body = ROTATION_DATES_RE.find(source)?.groupValues?.get(1) ?: return emptyMap()
        return ROTATION_DATE_ENTRY_RE.findAll(body)
            .mapNotNull { match ->
                val year = match.groupValues[1]
                val date = if (match.groupValues[2].isNotBlank()) {
                    LocalDate.of(
                        match.groupValues[2].toInt(),
                        match.groupValues[3].toInt(),
                        match.groupValues[4].toInt(),
                    )
                } else {
                    LocalDate.MIN
                }
                year.takeIf { date <= today }?.let { it to date }
            }
            .toMap()
    }

    private fun parseCardSetTokens(body: String): Set<String> =
        body.lineSequence()
            .map { it.substringBefore('#').trim().trimEnd(',').trim() }
            .filter { it.isNotEmpty() }
            .flatMap { CARD_SET_TOKEN_RE.findAll(it).map { match -> match.groupValues[1] } }
            .toSet()

    fun parseCardSetEnum(source: String): Set<String> {
        val header = CARDSET_HEADER_RE.find(source) ?: return emptySet()
        val rest = source.substring(header.range.last + 1)
        val out = linkedSetOf<String>()
        for (rawLine in rest.lineSequence()) {
            // Stop when we hit the next top-level `class …` definition.
            if (CLASS_HEADER_RE.containsMatchIn(rawLine) && !rawLine.startsWith("\t") && !rawLine.startsWith("    ")) {
                if (out.isNotEmpty()) break
            }
            val line = rawLine.substringBefore('#')
            val m = ENUM_MEMBER_RE.find(line) ?: continue
            out += m.groupValues[1]
        }
        return out
    }
}
