package com.home.widget.providers.spotify

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
)

@Serializable
data class SpotifySearchResponse(val tracks: SpotifyTracksDto? = null)

@Serializable
data class SpotifyTracksDto(val items: List<SpotifyTrackDto> = emptyList())

@Serializable
data class SpotifyTrackDto(
    val id: String,
    val name: String,
    val uri: String,
    @SerialName("duration_ms") val durationMs: Long,
    val artists: List<SpotifyArtistDto> = emptyList(),
    val album: SpotifyAlbumDto? = null,
)

@Serializable
data class SpotifyArtistDto(val name: String)

@Serializable
data class SpotifyAlbumDto(val images: List<SpotifyImageDto> = emptyList())

@Serializable
data class SpotifyImageDto(val url: String, val height: Int? = null, val width: Int? = null)
