package com.home.widget.providers.chatgpt

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import com.home.widget.BuildConfig
import com.home.widget.core.icons.AppIconResolver
import com.home.widget.core.network.NetworkModule
import com.home.widget.core.provider.ActionOutcome
import com.home.widget.core.provider.Provider
import com.home.widget.core.provider.ProviderResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ChatGPT -> answer. Smallest useful unit: a short, direct answer with a way
 * to keep going. There is no public API for the ChatGPT app itself to answer
 * inline, so the short answer is generated via the OpenAI Chat Completions
 * API (bring-your-own key, billed to your own OpenAI account). Without a key
 * — or if the call fails — the provider preserves the prompt and hands off
 * straight to ChatGPT, exactly as the spec requires: never simulate an
 * answer we don't actually have.
 */
class ChatGptProvider : Provider {
    override val id = "chatgpt"
    override val displayName = "ChatGPT"
    override val packageName = "com.openai.chatgpt"

    private val api: OpenAiApi by lazy {
        NetworkModule.retrofit(OpenAiApi.BASE_URL).create(OpenAiApi::class.java)
    }

    override fun icon(context: Context): Drawable? = AppIconResolver.icon(context, packageName)

    override fun isInstalled(context: Context): Boolean = AppIconResolver.isInstalled(context, packageName)

    override suspend fun search(context: Context, query: String): ProviderResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        if (apiKey.isBlank()) return@withContext ProviderResult.Unavailable("No OpenAI API key configured")

        try {
            val response = api.createChatCompletion(
                bearerToken = "Bearer $apiKey",
                request = ChatCompletionRequest(
                    messages = listOf(
                        ChatMessageDto(
                            role = "system",
                            content = "Answer in at most two short sentences. Be direct, no preamble.",
                        ),
                        ChatMessageDto(role = "user", content = query),
                    ),
                ),
            )
            val choice = response.choices.firstOrNull()
                ?: return@withContext ProviderResult.Unavailable("No answer returned")
            val text = choice.message.content.trim()
            if (text.isEmpty()) return@withContext ProviderResult.Unavailable("Empty answer")
            ProviderResult.Answer(text = text, truncated = choice.finishReason == "length")
        } catch (e: Exception) {
            ProviderResult.Unavailable("ChatGPT request failed: ${e.message}")
        }
    }

    override suspend fun performAction(context: Context, actionId: String, query: String): ActionOutcome {
        return when (actionId) {
            "continue" -> {
                openFullApp(context, query)
                ActionOutcome.LaunchedFullApp(if (isInstalled(context)) packageName else null)
            }
            else -> ActionOutcome.Failed("Unknown action")
        }
    }

    override fun openFullApp(context: Context, query: String) {
        if (isInstalled(context)) {
            // The ChatGPT app does not publish a documented deep-link scheme for
            // prefilling a prompt, so this is a best-effort launch: it opens the
            // app itself (not a fabricated "prefilled" experience). The web
            // fallback below is the reliable, documented way to hand off a query.
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return
            }
        }
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com/?q=${Uri.encode(query)}&hints=search"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
