package com.lvsmsmch.deckbuilder.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedDeckDao(): SavedDeckDao
    abstract fun hsJsonCardDao(): HsJsonCardDao

    companion object {
        const val NAME = "deck_builder.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}
