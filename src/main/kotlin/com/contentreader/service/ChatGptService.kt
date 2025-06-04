package com.contentreader.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.beans.factory.annotation.Value
import reactor.core.publisher.Mono

@Service
class ChatGptService(
    private val webClient: WebClient = WebClient.create("https://api.openai.com/v1")
) {
    @Value("\${openai.api.key}")
    private lateinit var apiKey: String

    fun askChatGpt(prompt: String): Mono<String> {
        val requestBody = mapOf(
            "model" to "gpt-4o",
            "messages" to listOf(mapOf("role" to "user", "content" to prompt))
        )
        return webClient.post()
            .uri("/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { (it["choices"] as List<Map<String, Any>>)[0]["message"].let { msg -> (msg as Map<*, *>)["content"].toString() } }
    }
}

