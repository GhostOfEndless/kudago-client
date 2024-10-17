package client

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.client.KudaGoClientImpl
import org.example.dto.News
import org.example.dto.NewsResponse

class KudaGoClientImplTest : StringSpec({

    "getNewsPage should return list of News on successful response" {
        val mockEngine = MockEngine { request ->
            respond(
                content = Json.encodeToString(
                    NewsResponse(
                        count = 10,
                        next = "",
                        previous = "",
                        results = listOf(
                            News(1, "News 1", "place 1", "description 1", "https://example.com/1", 1, 1,
                                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())),
                            News(2, "News 2", "place 2", "description 2", "https://example.com/2", 2, 2,
                                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
                        )
                    )
                ),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val kudaGoClient = KudaGoClientImpl(
            client = client,
            baseUrl = "https://example.com",
            maxRetries = 3,
            retryDelay = 100L
        )

        val result = runBlocking { kudaGoClient.getNewsPage(page = 1, pageSize = 2) }

        result.size shouldBe 2
        result[0].id shouldBe 1
        result[0].title shouldBe "News 1"
        result[1].id shouldBe 2
        result[1].title shouldBe "News 2"
    }

    "getNewsPage should return empty list after max retries on failure" {
        val mockEngine = MockEngine { request ->
            respondError(HttpStatusCode.InternalServerError)
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val kudaGoClient = KudaGoClientImpl(
            client = client,
            baseUrl = "https://example.com",
            maxRetries = 3,
            retryDelay = 10L
        )

        val result = runBlocking { kudaGoClient.getNewsPage(page = 1, pageSize = 2) }

        result shouldBe emptyList()
    }

    "getAllNews should return combined list of News across multiple pages" {
        val totalNews = 5
        val pageSize = 2

        val mockEngine = MockEngine { request ->
            val page = request.url.parameters["page"]?.toIntOrNull() ?: 1
            val newsList = when (page) {
                1 -> listOf(
                    News(1, "News 1", "place 1", "description 1", "https://example.com/1", 1, 1,
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())),
                    News(2, "News 2", "place 2", "description 2", "https://example.com/2", 1, 1,
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
                )
                2 -> listOf(
                    News(3, "News 3", "place 3", "description 3", "https://example.com/3", 1, 1,
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())),
                    News(4, "News 4", "place 4", "description 4", "https://example.com/4", 1, 1,
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
                )
                3 -> listOf(
                    News(5, "News 5", "place 5", "description 5", "https://example.com/5", 1, 1,
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
                )
                else -> emptyList()
            }
            respond(
                content = Json.encodeToString(NewsResponse(
                    count = 10,
                    next = "",
                    previous = "",
                    results = newsList
                )),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val kudaGoClient = KudaGoClientImpl(
            client = client,
            baseUrl = "https://example.com",
            maxRetries = 3,
            retryDelay = 10L,
            maxPageSize = pageSize
        )

        val result = kudaGoClient.getAllNews(count = totalNews)

        result.size shouldBe totalNews
        result.map { it.id } shouldBe listOf(1, 2, 3, 4, 5)
    }

    "retryRequest should retry on failure and succeed on subsequent attempt" {
        var attempt = 0
        val mockEngine = MockEngine { request ->
            attempt++
            if (attempt < 2) {
                respondError(HttpStatusCode.InternalServerError)
            } else {
                respond(
                    content = Json.encodeToString(
                        NewsResponse(
                            count = 10,
                            next = "",
                            previous = "",
                            results = listOf(
                                News(1, "News 1", "place 1", "description 1", "https://example.com/1", 1, 1,
                                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
                            )
                        )
                    ),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                )
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val kudaGoClient = KudaGoClientImpl(
            client = client,
            baseUrl = "https://example.com",
            maxRetries = 3,
            retryDelay = 10L
        )

        val result = runBlocking { kudaGoClient.getNewsPage(page = 1, pageSize = 1) }

        result.size shouldBe 1
        result[0].id shouldBe 1
    }
})