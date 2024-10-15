package org.example

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDate
import org.example.client.KudaGoClientImpl
import org.example.client.KudaGoCoroutineFlow
import org.example.dto.News
import org.example.util.NewsPrinter
import org.example.util.file.NewsFileManagerImpl
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

suspend fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val totalNewsCount = 10_000
    val workerCount = 3

    val kudaGoClient = KudaGoClientImpl(client)
    val newsFileManager = NewsFileManagerImpl()

    logger.info { "Downloading news from API..." }
    val newsList = kudaGoClient.getAllNews(totalNewsCount)
    val period = LocalDate(2023, 10, 31)..LocalDate(2024, 10, 31)
    logger.info { "Filtering downloaded news..." }
    val filteredNews = newsList.getMostRatedNews(10, period)
    logger.info { "Saving news to file..." }
    newsFileManager.saveNews("news.csv", filteredNews)

    printNews(filteredNews)

    val flow = KudaGoCoroutineFlow(kudaGoClient, totalNewsCount, workerCount)

    val time = measureTimeMillis {
        flow.execute()
    }

    println("Execution completed in $time ms")
}

fun List<News>.getMostRatedNews(
    count: Int,
    period: ClosedRange<LocalDate>
): List<News> {
    return this.filter { news ->
        news.publicationDate.date in period
    }.sortedByDescending { it.rating }.take(count)
}

fun printNews(newsList: List<News>) {
    val printer = NewsPrinter()
    newsList.forEach { news -> printer.formatNews(news) }
    println(printer.build())
}
