package com.lvsmsmch.deckbuilder.data.network

import com.lvsmsmch.deckbuilder.data.network.dto.CardBackSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface HearthstoneApi {

    @GET("hearthstone/cardbacks")
    suspend fun cardBacks(
        @QueryMap params: Map<String, String>,
    ): CardBackSearchResponseDto
}
