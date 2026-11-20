package com.home.widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import com.home.widget.R
import com.home.widget.core.provider.ProviderRegistry

/**
 * The real Home Screen widget. RemoteViews can only host a static layout —
 * a tap target, not a text field or an animated, network-backed snippet —
 * so this collapsed state is genuinely all a widget can be: the search bar
 * and the five real, installed app icons. Any tap opens [WidgetHostActivity],
 * which is where the actual expand/collapse experience lives. See
 * README.md "How the expand animation really works" for why.
 */
class HomeAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, buildRemoteViews(context)) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_REPLACED -> {
                // A provider app was installed/uninstalled: refresh so the icon
                // row always reflects reality instead of a stale bitmap.
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, HomeAppWidgetProvider::class.java))
                ids.forEach { id -> manager.updateAppWidget(id, buildRemoteViews(context)) }
            }
        }
    }

    private fun buildRemoteViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_collapsed)

        views.setOnClickPendingIntent(
            R.id.widget_search_bar,
            openHostActivityPendingIntent(context, requestCode = 0, providerId = null),
        )

        val iconViewIds = listOf(
            R.id.widget_icon_0, R.id.widget_icon_1, R.id.widget_icon_2, R.id.widget_icon_3, R.id.widget_icon_4,
        )
        ProviderRegistry.providers.forEachIndexed { index, provider ->
            val viewId = iconViewIds.getOrNull(index) ?: return@forEachIndexed
            val icon = provider.icon(context)
            if (icon != null) {
                views.setImageViewBitmap(viewId, icon.toBitmap(width = 96, height = 96))
            } else {
                views.setImageViewResource(viewId, R.drawable.ic_get_app)
            }
            views.setContentDescription(viewId, provider.displayName)
            views.setOnClickPendingIntent(
                viewId,
                openHostActivityPendingIntent(context, requestCode = index + 1, providerId = provider.id),
            )
        }
        return views
    }

    private fun openHostActivityPendingIntent(context: Context, requestCode: Int, providerId: String?): PendingIntent {
        val intent = Intent(context, WidgetHostActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            providerId?.let { putExtra(WidgetHostActivity.EXTRA_PROVIDER_ID, it) }
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
