package com.home.widget.providers.spotify

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import com.home.widget.core.icons.AppIconResolver
import com.home.widget.core.network.NetworkModule
import com.home.widget.core.provider.ActionOutcome
import com.home.widget.core.provider.Provider
import com.home.widget.core.provider.ProviderResult
import com.home.widget.core.provider.ResultAction
import com.home.widget.core.provider.ResultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Spotify -> songs. Smallest useful unit: the 5 closest-matching tracks, one
 * tap from playing. Search uses the Client Credentials flow (public catalog
 * data, no user login). Playback is started via the `spotify:` URI, which
 * hands off to the Spotify app — true in-place playback that never leaves
 * Home would require the Spotify App Remote SDK (separate app auth, native
 * library, Premium-only); out of scope for this MVP, and not something we
 * fake here.
 */
class SpotifyProvider : Provider {
    override val id = "spotify"
    override val displayName = "Spotify"
    override val packageName = "com.spotify.music"

    private val auth = SpotifyAuth()
    private val api: SpotifySearchApi by lazy {
        NetworkModule.retrofit(SpotifySearchApi.BASE_URL).create(SpotifySearchApi::class.java)
    }

    override fun icon(context: Context): Drawable? = AppIconResolver.icon(context, packageName)

    override fun isInstalled(context: Context): Boolean = AppIconResolver.isInstalled(context, packageName)

    override suspend fun search(context: Context, query: String): ProviderResult = withContext(Dispatchers.IO) {
        val token = auth.bearerToken() ?: return@withContext ProviderResult.Unavailable("No Spotify credentials configured")
        try {
            val response = api.search(bearerToken = "Bearer $token", query = query)
            val items = (response.tracks?.items ?: emptyList()).take(5).map { it.toResultItem() }
            if (items.isEmpty()) ProviderResult.Empty else ProviderResult.TopFive(items)
        } catch (e: Exception) {
            ProviderResult.Unavailable("Spotify search failed: ${e.message}")
        }
    }

    private fun SpotifyTrackDto.toResultItem(): ResultItem = ResultItem(
        id = id,
        title = name,
        subtitle = artists.joinToString(", ") { it.name },
        badge = formatDuration(durationMs),
        imageUrl = album?.images?.minByOrNull { (it.width ?: Int.MAX_VALUE) }?.url,
        actions = listOf(ResultAction(id = "play:$uri", label = "Play", isPrimary = true)),
    )

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    override suspend fun performAction(context: Context, actionId: String, query: String): ActionOutcome {
        val (verb, payload) = actionId.split(":", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
        return when (verb) {
            "play" -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payload)).apply {
                    setPackage(packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    ActionOutcome.LaunchedFullApp(packageName)
                } else {
                    openFullApp(context, query)
                    ActionOutcome.LaunchedFullApp(packageName)
                }
            }
            else -> ActionOutcome.Failed("Unknown action")
        }
    }

    override fun openFullApp(context: Context, query: String) {
        val spotifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${Uri.encode(query)}")).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (spotifyIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(spotifyIntent)
        } else {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/${Uri.encode(query)}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
