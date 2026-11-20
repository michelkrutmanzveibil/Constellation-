package com.home.widget.providers.youtube

import kotlinx.serialization.Serializable

@Serializable
data class YouTubeSearchResponse(val items: List<YouTubeSearchItemDto> = emptyList())

@Serializable
data class YouTubeSearchItemDto(val id: YouTubeVideoIdDto, val snippet: YouTubeSnippetDto)

@Serializable
data class YouTubeVideoIdDto(val videoId: String? = null)

@Serializable
data class YouTubeSnippetDto(
    val title: String,
    val channelTitle: String,
    val thumbnails: YouTubeThumbnailsDto,
)

@Serializable
data class YouTubeThumbnailsDto(
    val default: YouTubeThumbnailDto? = null,
    val medium: YouTubeThumbnailDto? = null,
)

@Serializable
data class YouTubeThumbnailDto(val url: String)

@Serializable
data class YouTubeVideosResponse(val items: List<YouTubeVideoItemDto> = emptyList())

@Serializable
data class YouTubeVideoItemDto(val id: String, val contentDetails: YouTubeContentDetailsDto)

@Serializable
data class YouTubeContentDetailsDto(val duration: String)
