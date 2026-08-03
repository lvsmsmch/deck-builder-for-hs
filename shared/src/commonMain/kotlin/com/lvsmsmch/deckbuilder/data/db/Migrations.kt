package com.lvsmsmch.deckbuilder.data.db

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * v6 adds `searchText` to the card table. Existing rows cannot be backfilled
 * correctly (SQLite's LOWER() does not fold non-ASCII), so the card cache is
 * cleared and re-downloaded on next launch. Saved decks live in another table
 * and are deliberately left untouched.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DELETE FROM hsjson_cards")
        connection.execSQL("ALTER TABLE hsjson_cards ADD COLUMN searchText TEXT NOT NULL DEFAULT ''")
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS index_hsjson_cards_locale_collectible " +
                "ON hsjson_cards (locale, collectible)",
        )
    }
}

/** Every migration the database knows about, in order. */
val AppDatabaseMigrations = arrayOf(MIGRATION_5_6)
