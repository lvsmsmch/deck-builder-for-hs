package com.lvsmsmch.deckbuilder.data.hsjson

import com.lvsmsmch.deckbuilder.data.hsjson.dto.HsJsonCardDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * HearthstoneJSON CDN. Base URL: https://api.hearthstonejson.com/v1/
 *
 * Build numbers: `latest` is a redirect to the current numeric build directory
 * (e.g. /v1/229145/enUS/cards.collectible.json). [BuildChecker] resolves the
 * latest build number; this api fetches a snapshot for a known build + locale.
 */
interface HsJsonApi {

    /** Cards JSON for a specific build + locale (HearthstoneJSON locale form: `enUS`, `ruRU`). */
    @GET("v1/{build}/{locale}/cards.collectible.json")
    suspend fun cardsForBuild(
        @Path("build") build: String,
        @Path("locale") locale: String,
    ): List<HsJsonCardDto>
}
