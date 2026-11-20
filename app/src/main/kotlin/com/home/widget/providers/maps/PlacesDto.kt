package com.home.widget.providers.maps

import kotlinx.serialization.Serializable

/** Wire types for the Places API (New) `places:searchText` endpoint. */
@Serializable
data class PlacesSearchRequest(
    val textQuery: String,
    val maxResultCount: Int = 5,
    val locationBias: LocationBiasDto? = null,
)

@Serializable
data class LocationBiasDto(val circle: CircleDto)

@Serializable
data class CircleDto(val center: LatLngDto, val radius: Double)

@Serializable
data class LatLngDto(val latitude: Double, val longitude: Double)

@Serializable
data class PlacesSearchResponse(val places: List<PlaceDto> = emptyList())

@Serializable
data class PlaceDto(
    val id: String,
    val displayName: DisplayNameDto? = null,
    val formattedAddress: String? = null,
    val location: LatLngDto? = null,
    val rating: Double? = null,
)

@Serializable
data class DisplayNameDto(val text: String)
