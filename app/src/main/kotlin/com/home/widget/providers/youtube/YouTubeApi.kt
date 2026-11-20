package com.home.widget.providers.youtube

import retrofit2.http.GET
import retrofit2.http.Query

/** https://developers.google.com/youtube/v3/docs/search/list and /videos/list */
interface YouTubeApi {
    @GET("youtube/v3/search")
    suspend fun search(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 5,
        @Query("safeSearch") safeSearch: String = "moderate",
    ): YouTubeSearchResponse

    @GET("youtube/v3/videos")
    suspend fun videos(
        @Query("key") apiKey: String,
        @Query("id") commaSeparatedIds: String,
        @Query("part") part: String = "contentDetails",
    ): YouTubeVideosResponse

    companion object {
        const val BASE_URL = "https://www.googleapis.com/"
    }
}
