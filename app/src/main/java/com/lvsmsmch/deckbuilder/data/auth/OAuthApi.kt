package com.lvsmsmch.deckbuilder.data.auth

import com.lvsmsmch.deckbuilder.data.network.dto.TokenDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface OAuthApi {

    /**
     * `client_credentials` grant. The Basic header is built from
     * `base64(CLIENT_ID:CLIENT_SECRET)` and passed in by [TokenCache].
     */
    @FormUrlEncoded
    @POST("token")
    suspend fun token(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "client_credentials",
    ): TokenDto
}
