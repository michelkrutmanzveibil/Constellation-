package com.home.widget.providers.uber

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.net.Uri
import com.home.widget.core.icons.AppIconResolver
import com.home.widget.core.provider.ActionOutcome
import com.home.widget.core.provider.Provider
import com.home.widget.core.provider.ProviderResult
import com.home.widget.core.provider.ResultAction
import com.home.widget.core.provider.ResultCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Uber -> ride. Smallest useful unit: confirm the destination, one tap from
 * a ride request with pickup pre-filled to the device's location. Destination
 * resolution uses the on-device [Geocoder] (no API key needed). Uber's real
 * price and ETA require the OAuth Ride Request API (a full per-user login
 * flow) which is out of scope for this MVP — rather than invent numbers,
 * the card shows only what's real (destination + distance context) and the
 * "Request Ride" action hands off to Uber, which shows its own live price
 * and ETA.
 */
class UberProvider : Provider {
    override val id = "uber"
    override val displayName = "Uber"
    override val packageName = "com.ubercab"

    override fun icon(context: Context): Drawable? = AppIconResolver.icon(context, packageName)

    override fun isInstalled(context: Context): Boolean = AppIconResolver.isInstalled(context, packageName)

    override suspend fun search(context: Context, query: String): ProviderResult = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) {
            return@withContext ProviderResult.Unavailable("No geocoder available on this device")
        }
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val address = geocoder.getFromLocationName(query, 1)?.firstOrNull()
                ?: return@withContext ProviderResult.Unavailable("Could not resolve \"$query\" to a destination")

            val formatted = (0..address.maxAddressLineIndex)
                .mapNotNull { address.getAddressLine(it) }
                .firstOrNull()
                ?: listOfNotNull(address.thoroughfare, address.locality, address.adminArea)
                    .joinToString(", ")

            val payload = "${address.latitude},${address.longitude},${Uri.encode(formatted)}"
            val card = ResultCard(
                title = address.featureName?.takeIf { it.isNotBlank() && it != formatted } ?: query,
                subtitle = formatted,
                details = emptyList(),
                actions = listOf(ResultAction(id = "request:$payload", label = "Request Ride", isPrimary = true)),
            )
            ProviderResult.SingleCard(card)
        } catch (e: Exception) {
            ProviderResult.Unavailable("Destination lookup failed: ${e.message}")
        }
    }

    override suspend fun performAction(context: Context, actionId: String, query: String): ActionOutcome {
        val (verb, payload) = actionId.split(":", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
        return when (verb) {
            "request" -> {
                val parts = payload.split(",", limit = 3)
                val lat = parts.getOrNull(0)?.toDoubleOrNull()
                val lng = parts.getOrNull(1)?.toDoubleOrNull()
                val address = parts.getOrNull(2)?.let { Uri.decode(it) } ?: query
                val uri = UberDeepLink.requestRide(lat, lng, address)
                val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                ActionOutcome.LaunchedFullApp(if (isInstalled(context)) packageName else null)
            }
            else -> ActionOutcome.Failed("Unknown action")
        }
    }

    override fun openFullApp(context: Context, query: String) {
        val uri = UberDeepLink.requestRide(dropoffLatitude = null, dropoffLongitude = null, dropoffAddress = query)
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
