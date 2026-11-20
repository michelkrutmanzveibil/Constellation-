package com.home.widget.providers.chatgpt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<ChatMessageDto>,
    @SerialName("max_tokens") val maxTokens: Int = 120,
    val temperature: Double = 0.4,
)

@Serializable
data class ChatMessageDto(val role: String, val content: String)

@Serializable
data class ChatCompletionResponse(val choices: List<ChatChoiceDto> = emptyList())

@Serializable
data class ChatChoiceDto(
    val message: ChatMessageDto,
    @SerialName("finish_reason") val finishReason: String? = null,
)
