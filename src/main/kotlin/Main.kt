package org.example

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDate
import org.example.client.KudaGoClientImpl
import org.example.dto.News
import org.example.util.NewsPrinter
import org.example.util.file.NewsFileManagerImpl

fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val kudaGoClient = KudaGoClientImpl(client)
    val newsFileManager = NewsFileManagerImpl()

    val newsList = kudaGoClient.getNews(1000)
    val period = LocalDate(2023, 10, 31)..LocalDate(2024, 10, 31)
    val filteredNews = newsList.getMostRatedNews(10, period)
    newsFileManager.saveNews("news.csv", filteredNews)

    printNews(filteredNews) { news ->
        header(1) { bold(news.title) }

        text { "Дата публикации: ${news.publicationDate}" }
        text { "Местоположение: ${underlined(news.place)}" }

        header(2) { "Описание" }
        text { news.description }

        header(2) { "Статистика" }
        text { "Закладки: ${news.favoritesCount}" }
        text { "Комментарии: ${news.commentsCount}" }
        text { "Рейтинг: ${String.format("%.2f", news.rating)}" }

        text { "Читать в источнике: ${link(news.siteUrl, "ссылка")}" }
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
