package com.contentreader.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.beans.factory.annotation.Value
import reactor.core.publisher.Mono
import com.contentreader.model.TranslatedText

@Service
class ChatGptService {
    private val webClient: WebClient = WebClient.create("https://api.openai.com/v1")
    @Value("\${openai.api.key}")
    private lateinit var apiKey: String

    fun askChatGpt(prompt: String): Mono<String> {
        val requestBody = mapOf(
            "model" to "gpt-4o",
            "messages" to listOf(mapOf("role" to "user", "content" to prompt)),
            "temperature" to 0,
            "top_p" to 1
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

    fun getTranslationCommand(content: List<String>): String {
        val formattedContent = content.joinToString(prefix = "[", postfix = "]", separator = ";; ") { "\"$it\"" }
        val command = """
            for array of $formattedContent return hiragana and English translation in this format.
            hiragana|English translation;
            No other confirmation text or symbol. Return only one line per element of array separated by ";;"
        """.trimIndent()
        println("Generated command: \n$command")
        return command
    }
    
    fun parseChatGptResponse(response: String): List<TranslatedText> {
        println("Parsing response: \n$response")
        val result = response.split(";").map { it.trim() }.filter { it.isNotEmpty() }.map {
            val parts = it.split("|")
            TranslatedText(parts[0], parts[1])
        }
        println("Parsed result: $result")
        return result
    }
}