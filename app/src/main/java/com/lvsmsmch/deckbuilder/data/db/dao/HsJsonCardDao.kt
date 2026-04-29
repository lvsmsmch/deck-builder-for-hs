package com.lvsmsmch.deckbuilder.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity

@Dao
interface HsJsonCardDao {

    @Query("SELECT COUNT(*) FROM hsjson_cards WHERE locale = :locale")
    suspend fun count(locale: String): Int

    @Query("SELECT * FROM hsjson_cards WHERE locale = :locale")
    suspend fun all(locale: String): List<HsJsonCardEntity>

    @Query("SELECT * FROM hsjson_cards WHERE locale = :locale AND dbfId = :dbfId LIMIT 1")
    suspend fun byDbfId(locale: String, dbfId: Int): HsJsonCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<HsJsonCardEntity>)

    @Query("DELETE FROM hsjson_cards WHERE locale = :locale")
    suspend fun deleteLocale(locale: String)

    @Transaction
    suspend fun replaceLocale(locale: String, rows: List<HsJsonCardEntity>) {
        deleteLocale(locale)
        insertAll(rows)
    }
}
