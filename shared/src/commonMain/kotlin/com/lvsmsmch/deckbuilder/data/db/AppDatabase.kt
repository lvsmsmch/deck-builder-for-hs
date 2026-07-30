package com.lvsmsmch.deckbuilder.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.lvsmsmch.deckbuilder.data.db.dao.HsJsonCardDao
import com.lvsmsmch.deckbuilder.data.db.dao.SavedDeckDao
import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity
import com.lvsmsmch.deckbuilder.data.db.entity.SavedDeckEntity

@Database(
    entities = [
        SavedDeckEntity::class,
        HsJsonCardEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedDeckDao(): SavedDeckDao
    abstract fun hsJsonCardDao(): HsJsonCardDao

    companion object {
        const val NAME = "deck_builder.db"
    }
}

// Room's KSP generates the per-platform `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
