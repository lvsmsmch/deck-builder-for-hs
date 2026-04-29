package com.lvsmsmch.deckbuilder.data.rotation

/**
 * Parses `hearthstone/enums.py` from python-hearthstone:
 *
 * - [parseStandardSets] extracts the trailing `STANDARD_SETS = (CardSet.X, CardSet.Y, ...)`
 *   tuple. The tuple may span multiple lines and contain comments.
 * - [parseCardSetEnum] extracts every member name of the `class CardSet(IntEnum)` block,
 *   used for the cross-check that detects when enums.py lags behind a new release.
 */
internal object EnumsParser {

    private val STANDARD_RE = Regex("""STANDARD_SETS\s*=\s*\(([^)]*)\)""", RegexOption.DOT_MATCHES_ALL)
    private val CARDSET_HEADER_RE = Regex("""class\s+CardSet\s*\([^)]+\)\s*:""")
    private val ENUM_MEMBER_RE = Regex("""^\s*([A-Z][A-Z0-9_]*)\s*=\s*-?\d+""")
    private val CLASS_HEADER_RE = Regex("""^\s*class\s+\w+""")

    fun parseStandardSets(source: String): Set<String> {
        val match = STANDARD_RE.find(source) ?: return emptySet()
        val body = match.groupValues[1]
        return body.lineSequence()
            .map { it.substringBefore('#').trim().trimEnd(',').trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { token ->
                val name = token.removePrefix("CardSet.")
                name.takeIf { it.isNotEmpty() && it.all { c -> c.isUpperCase() || c.isDigit() || c == '_' } }
            }
            .toSet()
    }

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
