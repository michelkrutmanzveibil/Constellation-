package com.home.widget.providers.spotify

import android.util.Base64
import com.home.widget.BuildConfig
import com.home.widget.core.network.NetworkModule
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Client Credentials flow: app-only auth, no end-user Spotify login required
 * because search results are public catalog data. Token is cached in memory
 * and refreshed 30s before expiry.
 */
class SpotifyAuth {
    private val api by lazy {
        NetworkModule.retrofit(SpotifyAuthApi.BASE_URL).create(SpotifyAuthApi::class.java)
    }
    private val mutex = Mutex()
    private var cachedToken: String? = null
    private var expiresAtMs: Long = 0L

    /** Returns a bearer token, or null if no client credentials are configured. */
    suspend fun bearerToken(): String? {
        val clientId = BuildConfig.SPOTIFY_CLIENT_ID
        val clientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET
        if (clientId.isBlank() || clientSecret.isBlank()) return null

        return mutex.withLock {
            val now = System.currentTimeMillis()
            val existing = cachedToken
            if (existing != null && now < expiresAtMs) return@withLock existing

            val credentials = Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)
            val response = api.getToken(basicAuth = "Basic $credentials")
            cachedToken = response.accessToken
            expiresAtMs = now + (response.expiresIn * 1000L) - 30_000L
            response.accessToken
        }
    }
}
