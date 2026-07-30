package com.lvsmsmch.deckbuilder.data.rotation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lvsmsmch.deckbuilder.domain.entities.StandardRotation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val SEP = "|"

class RotationStore(
    private val store: DataStore<Preferences>,
) {
    private val standardKey = stringPreferencesKey("rotation_standard_csv")
    private val knownKey = stringPreferencesKey("rotation_known_csv")
    private val shaKey = stringPreferencesKey("rotation_sha")
    private val dateKey = stringPreferencesKey("rotation_committed_at")
    private val fetchedAtKey = longPreferencesKey("rotation_fetched_at_ms")

    suspend fun get(): StandardRotation? = store.data.map { prefs ->
        val standard = prefs[standardKey].toSetOrEmpty()
        val known = prefs[knownKey].toSetOrEmpty()
        val fetchedAt = prefs[fetchedAtKey] ?: return@map null
        if (standard.isEmpty()) return@map null
        StandardRotation(
            standardSets = standard,
            knownSets = known,
            sourceSha = prefs[shaKey],
            sourceCommittedAtIso = prefs[dateKey],
            fetchedAtMs = fetchedAt,
        )
    }.first()

    suspend fun put(rotation: StandardRotation) {
        store.edit { prefs ->
            prefs[standardKey] = rotation.standardSets.joinToString(SEP)
            prefs[knownKey] = rotation.knownSets.joinToString(SEP)
            rotation.sourceSha?.let { prefs[shaKey] = it }
            rotation.sourceCommittedAtIso?.let { prefs[dateKey] = it }
            prefs[fetchedAtKey] = rotation.fetchedAtMs
        }
    }

    private fun String?.toSetOrEmpty(): Set<String> =
        this?.takeIf { it.isNotBlank() }?.split(SEP)?.toSet().orEmpty()
}
