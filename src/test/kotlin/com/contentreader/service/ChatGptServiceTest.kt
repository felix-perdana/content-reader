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
    記者団の取材に応じる小泉進次郎農林水産相＝１２日午後、東京都千代田区=Minister of Agriculture, Forestry and Fisheries, Shinjiro Koizumi, responding to questions from the press corps on the 12th in the afternoon, Chiyoda ward, Tokyo;
    小泉進次郎農林水産相は12日、無関税で輸入できるミニマムアクセス（最低輸入量、MA）米のうち、主食用枠のコメについて、入札を今月27日に実施すると表明した。=On the 12th, Minister of Agriculture, Forestry and Fisheries, Shinjiro Koizumi, announced that a bid for the rice designated for staple food under the minimum access (MA) rice, which can be imported duty-free, will be held on the 27th of this month.;
    【毎日更新】スーパーでのコメの平均価格=【Updated daily】The average price of rice in supermarkets;
    例年は9月に行っているが、コメの価格高騰対策の一環として約3カ月前倒しする。=Although it is usually held in September, it will be moved up by about three months as part of measures to address the surge in rice prices.;
    9月には引き渡しが可能と説明した。=It was explained that delivery would be possible in September.;
    農水省で記者団の取材に応じた。=He responded to questions from the press corps at the Ministry of Agriculture, Forestry and Fisheries.;
    MA米は通常、最大10万トンが主食用の輸入枠として設定されており、複数回に分けて入札を実施している。=MA rice typically has a maximum import quota of 100,000 tons for staple food use, and bids are conducted in several rounds.;
    今回は6月の初回入札では3万トンを対象とし、その後の入札も前倒しで毎月実施することを想定している。=This time, the initial bid in June will target 30,000 tons, and subsequent bids are also expected to be moved up and held monthly.;
        """

        val result = service.parseChatGptResponse(response)
        println(result)
    }
}