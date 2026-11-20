package com.home.widget.core.provider

import com.home.widget.providers.chatgpt.ChatGptProvider
import com.home.widget.providers.maps.MapsProvider
import com.home.widget.providers.spotify.SpotifyProvider
import com.home.widget.providers.uber.UberProvider
import com.home.widget.providers.youtube.YouTubeProvider

/**
 * The only place that knows every concrete [Provider]. Everything else in
 * the app — the widget, the trampoline activity, the view model — depends
 * only on [Provider]. Adding a sixth provider is a one-line addition here.
 */
object ProviderRegistry {
    val providers: List<Provider> = listOf(
        MapsProvider(),
        SpotifyProvider(),
        YouTubeProvider(),
        UberProvider(),
        ChatGptProvider(),
    )

    fun byId(id: String): Provider? = providers.firstOrNull { it.id == id }
}
