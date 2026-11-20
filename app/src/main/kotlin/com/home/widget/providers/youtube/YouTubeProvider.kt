package com.home.widget.providers.youtube

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import com.home.widget.BuildConfig
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
 * YouTube -> videos. Smallest useful unit: the 5 closest-matching videos,
 * one tap from playing. Playback happens by handing off to the YouTube app
 * via its documented `vnd.youtube:<id>` deep link — Home does not embed a
 * video player. If the device supports the platform's public
 * Picture-in-Picture API and YouTube is installed, the YouTube app itself is
 * able to continue as PiP when the user navigates away from it; that is the
 * YouTube app's own capability, not something Home simulates.
 */
class YouTubeProvider : Provider {
    override val id = "youtube"
    override val displayName = "YouTube"
    override val packageName = "com.google.android.youtube"

    private val api: YouTubeApi by lazy {
        NetworkModule.retrofit(YouTubeApi.BASE_URL).create(YouTubeApi::class.java)
    }

    override fun icon(context: Context): Drawable? = AppIconResolver.icon(context, packageName)

    override fun isInstalled(context: Context): Boolean = AppIconResolver.isInstalled(context, packageName)

    fun devicePictureInPictureSupported(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    override suspend fun search(context: Context, query: String): ProviderResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        if (apiKey.isBlank()) return@withContext ProviderResult.Unavailable("No YouTube API key configured")

        try {
            val searchResponse = api.search(apiKey = apiKey, query = query)
            val videoIds = searchResponse.items.mapNotNull { it.id.videoId }.take(5)
            if (videoIds.isEmpty()) return@withContext ProviderResult.Empty

            val durations = try {
                api.videos(apiKey = apiKey, commaSeparatedIds = videoIds.joinToString(","))
                    .items.associate { it.id to parseIsoDuration(it.contentDetails.duration) }
            } catch (e: Exception) {
                emptyMap()
            }

            val items = searchResponse.items.mapNotNull { item ->
                val videoId = item.id.videoId ?: return@mapNotNull null
                ResultItem(
                    id = videoId,
                    title = item.snippet.title,
                    subtitle = item.snippet.channelTitle,
                    badge = durations[videoId],
                    imageUrl = item.snippet.thumbnails.medium?.url ?: item.snippet.thumbnails.default?.url,
                    actions = listOf(ResultAction(id = "play:$videoId", label = "Play", isPrimary = true)),
                )
            }.take(5)

            if (items.isEmpty()) ProviderResult.Empty else ProviderResult.TopFive(items)
        } catch (e: Exception) {
            ProviderResult.Unavailable("YouTube search failed: ${e.message}")
        }
    }

    /** Parses ISO-8601 durations like "PT3M33S" or "PT1H2M" into "m:ss" / "h:mm:ss". */
    private fun parseIsoDuration(iso: String): String? {
        val match = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?").matchEntire(iso) ?: return null
        val (h, m, s) = match.destructured
        val hours = h.toIntOrNull() ?: 0
        val minutes = m.toIntOrNull() ?: 0
        val seconds = s.toIntOrNull() ?: 0
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }

    override suspend fun performAction(context: Context, actionId: String, query: String): ActionOutcome {
        val (verb, payload) = actionId.split(":", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
        return when (verb) {
            "play" -> {
                val deepLink = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$payload")).apply {
                    setPackage(packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (deepLink.resolveActivity(context.packageManager) != null) {
                    context.startActivity(deepLink)
                    ActionOutcome.LaunchedFullApp(packageName)
                } else {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$payload"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    ActionOutcome.LaunchedFullApp(null)
                }
            }
            else -> ActionOutcome.Failed("Unknown action")
        }
    }

    override fun openFullApp(context: Context, query: String) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
