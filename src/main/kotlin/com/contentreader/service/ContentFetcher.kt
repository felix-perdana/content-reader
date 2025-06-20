package com.contentreader.service

import com.contentreader.model.ArticlePreview
import com.contentreader.model.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Service
import java.net.http.HttpClient
import java.time.Duration

@Service
class ContentFetcher {

    fun fetchHeadlines(url: String): List<ArticlePreview> {
        try {
            val doc: Document = connectToUrl(url)
            
            // Find the section with id=tabpanelTopics1
            val section = doc.select("section#tabpanelTopics1").first()
            
            // Extract list items with links and spans
            val items = section?.select("ul li")
                ?.takeWhile { li ->
                    val title = li.select("span").first().text()
                    !title.equals("もっと見る")
                }
                ?.map { li ->
                    val link = li.select("a").attr("href")
                    val title = li.select("span").first().text()
                    val fullLink = getLinkToFullArticle(link)
                    ArticlePreview(url = fullLink, title = title)
                } ?: emptyList()

            return items
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch content with Jsoup: ${e.message}")
        }
    }

    fun readFullArticle(url: String): List<String> {
        val doc = connectToUrl(url)
        val articleBody = doc.select("div.article_body p")
        println("articleBody: \n$articleBody")

        // Remove <a> tags and their text from each paragraph
        val result = articleBody.map { pElem ->
            val clone = pElem.clone()
            clone.select("a").remove()
            clone.wholeText()
        }
        .flatMap { it.split("\n\n") }
        .filter { it.isNotBlank() }
        .map { it.trim() }
        println("result: \n$result")
        return result
    }

    private fun getLinkToFullArticle(url: String): String {
        try {
            val doc: Document = connectToUrl(url)

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

    fun connectToUrl(url: String): Document {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(60000)
            .get()
    }
}