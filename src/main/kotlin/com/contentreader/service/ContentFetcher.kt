package com.contentreader.service

import com.contentreader.model.ArticlePreview
import com.contentreader.model.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import java.net.http.HttpClient
import java.time.Duration

@Service
class ContentFetcher {

    fun fetch(url: String): Array<ArticlePreview> {
        try {
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()
            
            // Find the section with id=tabpanelTopics1
            val section = doc.select("section#tabpanelTopics1").first()
            
            // Extract list items with links and spans
            val items = section?.select("ul li")
                ?.takeWhile { li ->
                    val title = li.select("span").first().text()
                    !title.equals("もっと見る", ignoreCase = true)
                }
                ?.map { li ->
                    val link = li.select("a").attr("href")
                    val title = li.select("span").first().text()
                    val fullLink = getLinkToFullArticle(link)
                    ArticlePreview(fullLink, title)
                } ?: emptyList()


            
            return items.toTypedArray()
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch content with Jsoup: ${e.message}")
        }
    }

    fun readFullArticle(url: String): String {
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(30000)
            .get()
        // Adjust selector as needed for your article body
        return doc.select("div.article_body p").joinToString("\n") { it.text() }
    }

    private fun getLinkToFullArticle(url: String): String {
        return try {
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()

            // Find the first <a> tag with href matching the pattern
            val linkToFullArticle = doc.select("a[href]")
                .map { it.attr("href") }
                .firstOrNull { href ->
                    href.startsWith("https://news.yahoo.co.jp/articles/") &&
                    !href.contains("/images/")
                }

            return linkToFullArticle ?: ""

        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch article body: ${e.message}")
        }
    }

     
    
    
}