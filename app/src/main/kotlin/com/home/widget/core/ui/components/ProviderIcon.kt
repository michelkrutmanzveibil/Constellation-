package com.home.widget.core.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.drawable.toBitmap
import com.home.widget.core.provider.Provider

/**
 * The provider's real, currently-installed adaptive icon as a Compose
 * [Painter] — never a substitute glyph. Null means the app isn't installed;
 * callers fall back to a neutral "not installed" affordance.
 */
@Composable
fun rememberProviderIconPainter(context: Context, provider: Provider, sizePx: Int = 108): Painter? {
    return remember(provider.id) {
        provider.icon(context)?.let { drawable ->
            BitmapPainter(drawable.toBitmap(width = sizePx, height = sizePx).asImageBitmap())
        }
    }
}
