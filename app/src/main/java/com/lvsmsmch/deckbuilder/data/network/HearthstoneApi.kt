package com.lvsmsmch.deckbuilder.data.network

import com.lvsmsmch.deckbuilder.data.network.dto.CardBackSearchResponseDto
import com.lvsmsmch.deckbuilder.data.network.dto.MetadataAllDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface HearthstoneApi {

    @GET("hearthstone/metadata")
    suspend fun metadata(
        @Query("locale") locale: String,
    ): MetadataAllDto

    @GET("hearthstone/cardbacks")
    suspend fun cardBacks(
        @QueryMap params: Map<String, String>,
    ): CardBackSearchResponseDto
}
