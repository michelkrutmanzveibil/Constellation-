package com.home.widget.providers.maps

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/** https://developers.google.com/maps/documentation/places/web-service/text-search */
interface PlacesApi {
    @POST("v1/places:searchText")
    suspend fun searchText(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String,
        @Body request: PlacesSearchRequest,
    ): PlacesSearchResponse

    companion object {
        const val BASE_URL = "https://places.googleapis.com/"
        const val FIELD_MASK =
            "places.id,places.displayName,places.formattedAddress,places.location,places.rating"
    }
}
