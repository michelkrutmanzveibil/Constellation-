package com.home.widget.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

data class DeviceLocation(val latitude: Double, val longitude: Double)

/**
 * Best-effort last-known location using the framework LocationManager
 * directly — deliberately no Play Services dependency for one signal. This
 * never requests a fresh GPS fix (too slow for a glanceable widget); when
 * there's no recent fix or no permission, providers just search without a
 * location bias rather than blocking on one.
 */
object DeviceLocationProvider {
    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    fun lastKnown(context: Context): DeviceLocation? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        var best: Location? = null
        for (provider in manager.allProviders) {
            val location = try {
                manager.getLastKnownLocation(provider)
            } catch (e: SecurityException) {
                null
            } ?: continue
            if (best == null || location.time > best.time) {
                best = location
            }
        }
        return best?.let { DeviceLocation(it.latitude, it.longitude) }
    }
}
