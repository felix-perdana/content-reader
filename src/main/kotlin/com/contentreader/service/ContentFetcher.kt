package com.contentreader.service

import com.contentreader.model.ArticlePreview
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import java.net.http.HttpClient
import java.time.Duration

@Service
class ContentFetcher {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    // 1. Using Jsoup (Good for HTML parsing)
    fun fetchWithJsoup(url: String): String {
        try {
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()
            
            // Find the section with id=tabpanelTopics1
            val section = doc.select("section#tabpanelTopics1").first()
            
            // Extract list items with links and spans
            val items = section?.select("ul li")?.map { li ->
                val link = li.select("a").attr("href")
                val text = li.select("span").first().text()
                ArticlePreview(link, text)
            } ?: emptyList()
            
            return items.joinToString("\n") { "(${it.title})" }
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch content with Jsoup: ${e.message}")
        }
    }
}