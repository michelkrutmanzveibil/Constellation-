package com.home.widget.providers.spotify

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/** https://developer.spotify.com/documentation/web-api/tutorials/client-credentials-flow */
interface SpotifyAuthApi {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun getToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "client_credentials",
    ): SpotifyTokenResponse

    companion object {
        const val BASE_URL = "https://accounts.spotify.com/"
    }
}

/** https://developer.spotify.com/documentation/web-api/reference/search */
interface SpotifySearchApi {
    @GET("v1/search")
    suspend fun search(
        @Header("Authorization") bearerToken: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 5,
    ): SpotifySearchResponse

    companion object {
        const val BASE_URL = "https://api.spotify.com/"
    }
}
