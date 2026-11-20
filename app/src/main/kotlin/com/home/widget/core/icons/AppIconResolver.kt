package com.home.widget.core.icons

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

/**
 * Resolves the OFFICIAL adaptive icon of an installed app via PackageManager.
 * Home never bundles or fabricates substitute icons for a provider — if the
 * app isn't installed, callers fall back to a neutral "not installed" glyph
 * and a Play Store hand-off, they never draw a fake brand icon.
 */
object AppIconResolver {
    fun isInstalled(context: Context, packageName: String): Boolean =
        try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    /** The app's real, currently-installed launcher icon, or null if it isn't installed. */
    fun icon(context: Context, packageName: String): Drawable? =
        try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
}
