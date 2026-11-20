package com.home.widget.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = HomePrimaryLight,
    onPrimary = HomeOnPrimaryLight,
    surface = HomeSurfaceLight,
    onSurface = HomeOnSurfaceLight,
    surfaceContainer = HomeSurfaceContainerLight,
)

private val DarkColors = darkColorScheme(
    primary = HomePrimaryDark,
    onPrimary = HomeOnPrimaryDark,
    surface = HomeSurfaceDark,
    onSurface = HomeOnSurfaceDark,
    surfaceContainer = HomeSurfaceContainerDark,
)

@Composable
fun HomeTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val useDarkTheme = isSystemInDarkTheme()
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && useDarkTheme -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        useDarkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
