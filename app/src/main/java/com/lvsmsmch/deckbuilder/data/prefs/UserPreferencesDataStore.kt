package com.lvsmsmch.deckbuilder.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val FILE = "user_prefs"

val Context.userPrefsStore: DataStore<Preferences> by preferencesDataStore(name = FILE)
