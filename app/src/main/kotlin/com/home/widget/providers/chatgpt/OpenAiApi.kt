package com.home.widget.providers.chatgpt

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/** https://platform.openai.com/docs/api-reference/chat/create */
interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") bearerToken: String,
        @Body request: ChatCompletionRequest,
    ): ChatCompletionResponse

    companion object {
        const val BASE_URL = "https://api.openai.com/"
    }
}
