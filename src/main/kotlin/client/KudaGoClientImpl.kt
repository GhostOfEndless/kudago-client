package org.example.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.example.dto.News
import org.example.dto.NewsResponse


class KudaGoClientImpl(
    private val client: HttpClient,
) : KudaGoClient {

    companion object {
        private const val BASE_URL = "https://kudago.com/public-api/v1.4/news/"
        private const val MAX_PAGE_SIZE = 100
    }

    private val logger = KotlinLogging.logger {}

    override fun getNews(count: Int): List<News> {
        return runBlocking {
            try {
                val newsList = mutableListOf<News>()
                val totalPages = (count + MAX_PAGE_SIZE - 1) / MAX_PAGE_SIZE

                for (page in 1..totalPages) {
                    val currentPageSize = if (page == totalPages) {
                        count - newsList.size
                    } else {
                        MAX_PAGE_SIZE
                    }

                    logger.debug { "Fetch page with number '$page' and size '$currentPageSize'" }
                    val response: HttpResponse = client.get(BASE_URL) {
                        parameter("location", "msk")
                        parameter("text_format", "text")
                        parameter("expand", "place")
                        parameter("fields", "id,title,place,description,site_url,favorites_count,comments_count,publication_date")
                        parameter("page_size", currentPageSize)
                        parameter("page", page)

                    }

                    when (response.status) {
                        HttpStatusCode.OK -> {
                            val newsResponse = response.body<NewsResponse>()
                            newsList.addAll(newsResponse.results)
                        }

                        else -> {
                            throw Exception("Unexpected HTTP status code: ${response.status}")
                        }
                    }
                }

                newsList
            } catch (e: Exception) {
                logger.error { "Error: ${e.message}" }
                emptyList()
            }
        }
    }
}