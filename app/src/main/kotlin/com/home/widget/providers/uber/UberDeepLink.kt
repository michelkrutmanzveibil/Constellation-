package com.home.widget.providers.uber

import android.net.Uri

/**
 * Uber's officially documented Trip Planning universal deep link
 * (https://developer.uber.com/docs/riders/ride-requests/tutorials/deep-links/introduction).
 * No OAuth or client id is required for the basic setPickup/dropoff prefill —
 * the Uber app (or uber.com as a fallback) opens ready to request the ride,
 * showing Uber's own real price and ETA. Home never fabricates those numbers
 * itself.
 */
object UberDeepLink {
    fun requestRide(dropoffLatitude: Double?, dropoffLongitude: Double?, dropoffAddress: String): Uri {
        val builder = Uri.parse("https://m.uber.com/ul/").buildUpon()
            .appendQueryParameter("action", "setPickup")
            .appendQueryParameter("pickup", "my_location")
            .appendQueryParameter("dropoff[formatted_address]", dropoffAddress)
        if (dropoffLatitude != null && dropoffLongitude != null) {
            builder
                .appendQueryParameter("dropoff[latitude]", dropoffLatitude.toString())
                .appendQueryParameter("dropoff[longitude]", dropoffLongitude.toString())
        }
        return builder.build()
    }
}
