package com.contentreader.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.ArgumentMatchers.any
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChatGptServiceTest {

    @Autowired
    private lateinit var service: ChatGptService

    //./gradlew test --tests "com.contentreader.service.ChatGptServiceTest.askChatGpt should make real API call to ChatGPT"
    @Test
    @Tag("integration")
    fun `askChatGpt should make real API call to ChatGPT`() {
        // Given
        val content = listOf(
            
  "記者団の取材に応じる小泉進次郎農林水産相＝１２日午後、東京都千代田区",
  "　小泉進次郎農林水産相は12日、無関税で輸入できるミニマムアクセス（最低輸入量、MA）米のうち、主食用枠のコメについて、入札を今月27日に実施すると表明した。 【毎日更新】スーパーでのコメの平均価格 　例年は9月に行っているが、コメの価格高騰対策の一環として約3カ月前倒しする。9月には引き渡しが可能と説明した。農水省で記者団の取材に応じた。 　MA米は通常、最大10万トンが主食用の輸入枠として設定されており、複数回に分けて入札を実施している。今回は6月の初回入札では3万トンを対象とし、その後の入札も前倒しで毎月実施することを想定している。　"

        )
        val prompt = service.getTranslationCommand(content)

        // When
        val result = service.askChatGpt(prompt)
        
        // Then
        StepVerifier.create(result)
            .assertNext { response: String ->
                assertTrue(response.isNotEmpty(), "Response should not be empty")
                println("Prompt: \n$prompt\n\n")
                println("--------------------------------")
                println("ChatGPT Response: \n$response")
            }
            .verifyComplete()
    }

    //./gradlew test --tests "com.contentreader.service.ChatGptServiceTest.parse chatgpt response"
    @Test
    fun `parse chatgpt response`() {
        val response = """
        """

        val result = service.parseChatGptResponse(response)
        println(result)
    }
}