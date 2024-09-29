package org.example

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDate
import org.example.client.KudaGoClientImpl
import org.example.dto.News
import org.example.util.NewsPrinter
import org.example.util.file.NewsFileManagerImpl

private val logger = KotlinLogging.logger {}

fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val kudaGoClient = KudaGoClientImpl(client)
    val newsFileManager = NewsFileManagerImpl()

    logger.info { "Downloading news from API..." }
    val newsList = kudaGoClient.getNews(10_000)
    val period = LocalDate(2023, 10, 31)..LocalDate(2024, 10, 31)
    logger.info { "Filtering downloaded news..." }
    val filteredNews = newsList.getMostRatedNews(10, period)
    logger.info { "Saving news to file..." }
    newsFileManager.saveNews("news.csv", filteredNews)

    printNews(filteredNews) {
        header(1) { bold(it.title) }

        text { "Дата публикации: ${it.publicationDate}" }
        text { "Местоположение: ${underlined(it.place)}" }

        header(2) { "Описание" }
        text { it.description }

        header(2) { "Статистика" }
        text { "Закладки: ${it.favoritesCount}" }
        text { "Комментарии: ${it.commentsCount}" }
        text { "Рейтинг: ${String.format("%.2f", it.rating)}" }

        text { "Читать в источнике: ${link(it.siteUrl, "ссылка")}" }
    }
}

fun List<News>.getMostRatedNews(
    count: Int,
    period: ClosedRange<LocalDate>
): List<News> {
    return this.filter { news ->
        news.publicationDate.date in period
    }.sortedByDescending { it.rating }.take(count)
}

fun printNews(newsList: List<News>, block: NewsPrinter.(News) -> Unit) {
    val printer = NewsPrinter()
    newsList.forEachIndexed { index, news ->
        printer.block(news)
        if (index < newsList.size - 1) {
            printer.divider()
        }
    }
    println(printer.build())
}
