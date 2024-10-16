package org.example.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.example.dto.News
import org.example.dto.NewsResponse


class KudaGoClientImpl(
    private val client: HttpClient,
    val baseUrl: String,
    val maxPageSize: Int = 100,
    val maxRetries: Int = 3,
    val retryDelay: Long = 1000L,
    maxConcurrentRequests: Int = 5
) : KudaGoClient {

    private val logger = KotlinLogging.logger {}
    private val semaphore = Semaphore(maxConcurrentRequests)

    suspend fun getNewsPage(page: Int, pageSize: Int): List<News> {
        return semaphore.withPermit {
            try {
                val response = retryRequest(page, pageSize)
                response.results
            } catch (e: Exception) {
                logger.error { "Failed to fetch news: ${e.message}" }
                emptyList()
            }
        }
    }

    override fun getAllNews(count: Int): List<News> {
        return runBlocking {
            try {
                val newsList = mutableListOf<News>()
                val totalPages = (count + maxPageSize - 1) / maxPageSize

                for (page in 1..totalPages) {
                    val currentPageSize = if (page == totalPages) {
                        count - newsList.size
                    } else {
                        maxPageSize
                    }

                    val newsResponse = retryRequest(page, currentPageSize)
                    newsList.addAll(newsResponse.results)
                }

                logger.info { "Successfully fetched data!" }
                newsList
            } catch (e: Exception) {
                logger.error { "Error: ${e.message}" }
                emptyList()
            }
        }
    }

    private suspend fun retryRequest(page: Int, pageSize: Int): NewsResponse {
        repeat(maxRetries) { attempt ->
            try {
                logger.debug { "Getting page with number '$page' and size '$pageSize' (Attempt ${attempt + 1})..." }
                val response: HttpResponse = client.get(baseUrl) {
                    parameter("location", "msk")
                    parameter("text_format", "text")
                    parameter("expand", "place")
                    parameter(
                        "fields",
                        "id,title,place,description,site_url,favorites_count,comments_count,publication_date"
                    )
                    parameter("page_size", pageSize)
                    parameter("page", page)
                }

                if (response.status == HttpStatusCode.OK) {
                    logger.debug { "Successfully fetched data from remote API!" }
                    return response.body<NewsResponse>()
                } else {
                    throw Exception("Unexpected HTTP status code: ${response.status}")
                }
            } catch (e: Exception) {
                logger.error { "Error on attempt ${attempt + 1}: ${e.message}" }
                if (attempt == maxPageSize - 1) throw e
                delay(retryDelay)
            }
        }
        throw Exception("Max retries number reached! Check your internet connection")
    }
}