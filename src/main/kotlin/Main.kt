package org.example

import com.typesafe.config.ConfigFactory
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
import java.io.File
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class AppConfig(configFile: String = "src/main/resources/application.conf") {

    private val config = ConfigFactory.parseFile(File(configFile)).resolve()

    val totalNewsCount: Int = config.getInt("app.totalNewsCount")
    val workerCount: Int = config.getInt("app.workerCount")
    val maxConcurrentRequests: Int = config.getInt("app.maxConcurrentRequests")
    val maxPageSize: Int = config.getInt("app.maxPageSize")
    val maxRetries: Int = config.getInt("app.maxRetries")
    val retryDelay: Long = config.getLong("app.retryDelay")
    val baseUrl: String = config.getString("app.baseUrl")
}


suspend fun main() {
    val config = AppConfig()
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val newsFileManager = NewsFileManagerImpl()
    val kudaGoClient = KudaGoClientImpl(
        client, config.baseUrl, config.maxPageSize,
        config.maxRetries, config.retryDelay, config.maxConcurrentRequests
    )

    logger.info { "Downloading news from API..." }
    val newsList = kudaGoClient.getAllNews(config.totalNewsCount)
    val period = LocalDate(2023, 10, 31)..LocalDate(2024, 10, 31)
    logger.info { "Filtering downloaded news..." }
    val filteredNews = newsList.getMostRatedNews(10, period)
    logger.info { "Saving news to file..." }
    newsFileManager.saveNews("news.csv", filteredNews)

    printNews(filteredNews)

    val flow = KudaGoCoroutineFlow(
        kudaGoClient, config.totalNewsCount,
        config.workerCount, config.maxPageSize
    )

    val time = measureTimeMillis {
        flow.execute()
    }

    logger.info { "Execution completed in $time ms" }
}

fun List<News>.getMostRatedNews(
    count: Int, period: ClosedRange<LocalDate>
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
