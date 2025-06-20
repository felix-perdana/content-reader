package com.contentreader.controller

import com.contentreader.service.ContentFetcher
import com.contentreader.service.ChatGptService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingle
import com.contentreader.model.ArticlePreview
import com.contentreader.model.TranslatedText
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ArrayNode
import java.util.concurrent.ConcurrentHashMap


@RestController
class ContentReaderController(
    private val contentFetcher: ContentFetcher,
    private val chatGptService: ChatGptService,
    private val objectMapper: ObjectMapper
) {
    // In-memory cache: Japanese text -> TranslatedText
    // LRU Cache implementation
    private class LruCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(16, 0.75f, true) {
        @Synchronized
        override fun get(key: K): V? = super.get(key)

        @Synchronized
        override fun put(key: K, value: V): V? = super.put(key, value)

        @Synchronized
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxSize
        }
    }

    private val translationCache = LruCache<String, TranslatedText>(1000)

    @GetMapping("/headlines", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getHeadlines(): ResponseEntity<List<ArticlePreview>> {
        val url = "https://www.yahoo.co.jp/"
        val headlines = contentFetcher.fetchHeadlines(url)

        return ResponseEntity.ok(headlines)
    }

    @PostMapping("/getProcessedText", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getProcessedText(
        @RequestBody text: List<String>
    ): ResponseEntity<List<TranslatedText>> {
        // Filter out texts that are already cached
        val uncachedTexts = text.filter { !translationCache.containsKey(it) }

        if (uncachedTexts.isNotEmpty()) {
            val content = chatGptService.getTranslationCommand(uncachedTexts)
            val chatGptResponse = chatGptService.askChatGpt(content).awaitSingle()
            
            val result = chatGptService.parseChatGptResponse(chatGptResponse)
            // Store new results in cache
            uncachedTexts.zip(result).forEach { (original, translated) ->
                translationCache[original] = translated
            }
        } 

        // Preserve input order
        val allResults = text.mapNotNull { translationCache[it] }
        return ResponseEntity.ok(allResults)
    }

     
    @GetMapping("/readNews", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun readNews(@RequestParam link: String): ResponseEntity<List<String>> {
        val articleBody = contentFetcher.readFullArticle(link)

        return ResponseEntity.ok(articleBody)
    }

    
    
}

