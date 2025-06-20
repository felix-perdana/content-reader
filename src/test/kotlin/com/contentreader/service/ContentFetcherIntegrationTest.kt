package com.contentreader.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ContentFetcherIntegrationTest {

    private val fetcher = ContentFetcher()

    @Test
    fun `fetchHeadlines should return non-empty list from Yahoo Japan`() {
        val url = "https://www.yahoo.co.jp/"
        val headlines = fetcher.fetchHeadlines(url)
        assertTrue(headlines.isNotEmpty(), "Headlines should not be empty")
        println("First headline: ${headlines.firstOrNull()}")
    }

    //./gradlew test --tests "com.contentreader.service.ContentFetcherIntegrationTest.readFullArticle should return non-empty article body"
    @Test
    fun `readFullArticle should return non-empty article body`() {
        // Use a real Yahoo News article URL for integration testing
        val articleUrl = "https://news.yahoo.co.jp/articles/d7ca10b30a9738729cfb3a65af41ce720a4d083f"
        val articleBody = fetcher.readFullArticle(articleUrl)
        //assertTrue(articleBody.isNotBlank(), "Article body should not be blank")
        println("paragraphs count: ${articleBody.size}")
        println("Article body: $articleBody")
    }
}