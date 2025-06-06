package com.contentreader.controller

import com.contentreader.service.ContentFetcher
import com.contentreader.service.ChatGptService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingle

@RestController
class HelloController(
    private val contentFetcher: ContentFetcher,
    private val chatGptService: ChatGptService,
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/headline", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun headline(): ResponseEntity<Any> {
        val url = "https://www.yahoo.co.jp/"
        val result = contentFetcher.fetch(url)
        val content = getTranslationCommand(result.joinToString("\n") { it.title })

        val chatGptResponse = chatGptService.askChatGpt(content).awaitSingle()
                            .replace("```json", "")
                            .replace("```", "")
                            .trim()

        val jsonNode = objectMapper.readTree(chatGptResponse)

        // Add link to each translation item
        val translationArray = jsonNode.get("translation")
        if (translationArray != null && translationArray.isArray) {
            for ((i, item) in translationArray.withIndex()) {
                if (i < result.size && item is com.fasterxml.jackson.databind.node.ObjectNode) {
                    item.put("link", result[i].link)
                }
            }
        }

        return ResponseEntity.ok(jsonNode)
    }

     @GetMapping("/readNews", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun readNews(link: String): ResponseEntity<Any> {
    
        val articleBody = contentFetcher.readFullArticle(link)
        val content = getTranslationCommandParagraph(articleBody)

        val chatGptResponse = chatGptService.askChatGpt(content).awaitSingle()
                            .replace("```json", "")
                            .replace("```", "")
                            .trim()

        val jsonNode = objectMapper.readTree(chatGptResponse)
        return ResponseEntity.ok(jsonNode)
    }

    private fun getTranslationCommand(content: String): String {
        return """
            Provide the english translation of this $content 
            along with the how-to-read in hiragana, break down per sentence,
            
            respond strictly in the following json format without any explanation, greeting, or extra message:
            {
                "translation": [
                    {
                        "original": "<Original Japanese>",
                        "reading": "<Reading in hiragana>",
                        "english": "<English translation>"
                    }
                ]
            }            
            Respond with this format for each item. Do not add anything else."            
        """.trimIndent()
    }

    private fun getTranslationCommandParagraph(content: String): String {
        return """
            Provide the english translation of this $content 
            along with the how-to-read in hiragana, break down per paragraph,
            
            respond strictly in the following json format without any explanation, greeting, or extra message:
            {
                "translation": [
                    {
                        "original": "<Original Japanese>",
                        "reading": "<Reading in hiragana>",
                        "english": "<English translation>"
                    }
                ]
            }            
            Respond with this format for each item. Do not add anything else."            
        """.trimIndent()
    }
}

