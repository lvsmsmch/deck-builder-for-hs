package com.lvsmsmch.deckbuilder.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import com.lvsmsmch.deckbuilder.data.db.entity.HsJsonCardEntity

@Dao
interface HsJsonCardDao {

    @Query("SELECT COUNT(*) FROM hsjson_cards WHERE locale = :locale")
    suspend fun count(locale: String): Int

    @Query("SELECT COALESCE(SUM(LENGTH(payloadJson)), 0) FROM hsjson_cards WHERE locale = :locale")
    suspend fun payloadChars(locale: String): Long

    @Query("SELECT * FROM hsjson_cards WHERE locale = :locale")
    suspend fun all(locale: String): List<HsJsonCardEntity>

    @Query("SELECT * FROM hsjson_cards WHERE locale = :locale AND dbfId = :dbfId LIMIT 1")
    suspend fun byDbfId(locale: String, dbfId: Int): HsJsonCardEntity?

    @Query("SELECT * FROM hsjson_cards WHERE locale = :locale AND dbfId IN (:dbfIds)")
    suspend fun byDbfIds(locale: String, dbfIds: List<Int>): List<HsJsonCardEntity>

    @Query("SELECT * FROM hsjson_cards WHERE locale = :locale AND cardId = :cardId LIMIT 1")
    suspend fun byCardId(locale: String, cardId: String): HsJsonCardEntity?

    @Query("SELECT * FROM hsjson_cards WHERE locale = :locale AND name = :name COLLATE NOCASE LIMIT 1")
    suspend fun byName(locale: String, name: String): HsJsonCardEntity?

    /** Distinct sets that carry collectible cards — used for the rotation cross-check. */
    @Query("SELECT DISTINCT cardSet FROM hsjson_cards WHERE locale = :locale AND collectible = 1 AND cardSet IS NOT NULL")
    suspend fun collectibleSets(locale: String): List<String>

    /** Filtering, sorting and paging happen in SQLite; see CardQuery. */
    @RawQuery
    suspend fun search(query: RoomRawQuery): List<HsJsonCardEntity>

    @RawQuery
    suspend fun count(query: RoomRawQuery): Int

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
