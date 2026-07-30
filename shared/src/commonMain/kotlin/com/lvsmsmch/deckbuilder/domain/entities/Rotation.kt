package com.lvsmsmch.deckbuilder.domain.entities

/**
 * Snapshot of Standard rotation as seen in `python-hearthstone/enums.py`.
 *
 * [standardSets] / [knownSets] hold raw `CardSet` enum tokens (e.g. `BLACK_TEMPLE`),
 * matching the values HearthstoneJSON puts in the `set` field of a card.
 */
data class StandardRotation(
    val standardSets: Set<String>,
    val knownSets: Set<String>,
    val sourceSha: String?,
    val sourceCommittedAtIso: String?,
    val fetchedAtMs: Long,
)

/**
 * Result of cross-checking the rotation snapshot against the live cards pool.
 *
 * [unknownSets] are tokens that appear on collectible cards but aren't listed in
 * `enums.py` at all — strong signal that python-hearthstone is lagging.
 */
data class RotationStatus(
    val rotation: StandardRotation,
    val unknownSets: Set<String>,
) {
    val isOutdated: Boolean get() = unknownSets.isNotEmpty()
}
