package com.lvsmsmch.deckbuilder.data.network

import com.lvsmsmch.deckbuilder.data.network.dto.CardBackSearchResponseDto
import com.lvsmsmch.deckbuilder.data.network.dto.CardDto
import com.lvsmsmch.deckbuilder.data.network.dto.CardSearchResponseDto
import com.lvsmsmch.deckbuilder.data.network.dto.DeckDto
import com.lvsmsmch.deckbuilder.data.network.dto.MetadataAllDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface HearthstoneApi {

    @GET("hearthstone/cards")
    suspend fun searchCards(
        @QueryMap params: Map<String, String>,
    ): CardSearchResponseDto

    @GET("hearthstone/cards/{idOrSlug}")
    suspend fun card(
        @Path("idOrSlug") idOrSlug: String,
        @Query("locale") locale: String,
        @Query("gameMode") gameMode: String? = null,
    ): CardDto

    @GET("hearthstone/metadata")
    suspend fun metadata(
        @Query("locale") locale: String,
    ): MetadataAllDto

    /** Decode an existing deck by its share code. Plan §10.1. */
    @GET("hearthstone/deck")
    suspend fun deckByCode(
        @Query("locale") locale: String,
        @Query("code") code: String,
    ): DeckDto

    /**
     * Assemble a deck from card ids + hero card id; the API returns the
     * canonical share code in [DeckDto.deckCode]. Plan §10.4.
     */
    @GET("hearthstone/deck")
    suspend fun deckByIds(
        @Query("locale") locale: String,
        @Query("ids") ids: String,
        @Query("hero") heroCardId: Int? = null,
    ): DeckDto

    @GET("hearthstone/cardbacks")
    suspend fun cardBacks(
        @QueryMap params: Map<String, String>,
    ): CardBackSearchResponseDto
}
