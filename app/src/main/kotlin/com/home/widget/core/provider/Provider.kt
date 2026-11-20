package com.home.widget.core.provider

import android.content.Context
import android.graphics.drawable.Drawable

/**
 * The single contract the widget speaks. The widget UI has no Maps/Spotify/
 * YouTube/Uber/ChatGPT specific code anywhere — it only ever calls through
 * this interface. Adding a sixth provider means implementing [Provider] and
 * registering it in `ProviderRegistry`; nothing else in the app changes.
 */
interface Provider {
    val id: String
    val displayName: String
    val packageName: String

    /** The app's real, installed adaptive icon. Null if the app isn't installed. */
    fun icon(context: Context): Drawable?

    fun isInstalled(context: Context): Boolean

    /** Performs the provider's single smallest useful action for [query]. */
    suspend fun search(context: Context, query: String): ProviderResult

    /** Executes an action id surfaced by a previous [search] result. */
    suspend fun performAction(context: Context, actionId: String, query: String): ActionOutcome

    /** Always available: hands off to the full app with the query preserved. */
    fun openFullApp(context: Context, query: String)
}
