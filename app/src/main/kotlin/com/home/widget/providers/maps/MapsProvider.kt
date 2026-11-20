package com.home.widget.providers.maps

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import androidx.core.net.toUri
import com.home.widget.BuildConfig
import com.home.widget.core.icons.AppIconResolver
import com.home.widget.core.location.DeviceLocationProvider
import com.home.widget.core.network.NetworkModule
import com.home.widget.core.provider.ActionOutcome
import com.home.widget.core.provider.Provider
import com.home.widget.core.provider.ProviderResult
import com.home.widget.core.provider.ResultAction
import com.home.widget.core.provider.ResultItem
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Maps -> places. Smallest useful unit: the 5 closest matches, each one tap
 * from turn-by-turn navigation. Uses the Places API (New) Text Search, biased
 * to the device's last-known location when permission is granted.
 */
class MapsProvider : Provider {
    override val id = "maps"
    override val displayName = "Maps"
    override val packageName = "com.google.android.apps.maps"

    private val api: PlacesApi by lazy {
        NetworkModule.retrofit(PlacesApi.BASE_URL).create(PlacesApi::class.java)
    }

    override fun icon(context: Context): Drawable? = AppIconResolver.icon(context, packageName)

    override fun isInstalled(context: Context): Boolean = AppIconResolver.isInstalled(context, packageName)

    override suspend fun search(context: Context, query: String): ProviderResult {
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isBlank()) return ProviderResult.Unavailable("No Places API key configured")

        return withContext(Dispatchers.IO) {
            try {
                val here = DeviceLocationProvider.lastKnown(context)
                val response = api.searchText(
                    apiKey = apiKey,
                    fieldMask = PlacesApi.FIELD_MASK,
                    request = PlacesSearchRequest(
                        textQuery = query,
                        maxResultCount = 5,
                        locationBias = here?.let {
                            LocationBiasDto(
                                CircleDto(center = LatLngDto(it.latitude, it.longitude), radius = 15_000.0)
                            )
                        },
                    ),
                )
                val items = response.places.take(5).map { place -> place.toResultItem(here) }
                if (items.isEmpty()) ProviderResult.Empty else ProviderResult.TopFive(items)
            } catch (e: Exception) {
                ProviderResult.Unavailable("Places lookup failed: ${e.message}")
            }
        }
    }

    private fun PlaceDto.toResultItem(here: com.home.widget.core.location.DeviceLocation?): ResultItem {
        val badgeParts = mutableListOf<String>()
        if (here != null && location != null) {
            badgeParts += formatDistanceMiles(here.latitude, here.longitude, location.latitude, location.longitude)
        }
        rating?.let { badgeParts += "★${"%.1f".format(it)}" }

        val actions = mutableListOf<ResultAction>()
        if (location != null) {
            actions += ResultAction(id = "navigate:${location.latitude},${location.longitude}", label = "Navigate", isPrimary = true)
        }
        return ResultItem(
            id = id,
            title = displayName?.text ?: "Unnamed place",
            subtitle = formattedAddress,
            badge = badgeParts.joinToString(" · ").ifBlank { null },
            actions = actions,
        )
    }

    private fun formatDistanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        val miles = results[0] / 1609.344
        return if (miles < 0.1) "<0.1 mi" else "%.1f mi".format(miles)
    }

    override suspend fun performAction(context: Context, actionId: String, query: String): ActionOutcome {
        val (verb, payload) = actionId.split(":", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
        return when (verb) {
            "navigate" -> {
                val (lat, lng) = payload.split(",").map { it.toDouble() }
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "google.navigation:q=$lat,$lng".toUri(),
                ).apply {
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
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(query))
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(if (isInstalled(context)) packageName else null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
