package org.example.util

import org.example.dto.News

class NewsPrinter {
    private val stringBuilder = StringBuilder()

    private fun header(level: Int, content: () -> String) {
        stringBuilder.append("#".repeat(level)).append(" ")
        stringBuilder.append(content()).append("\n\n")
    }

    private fun text(content: () -> String) {
        stringBuilder.append(content()).append("\n")
    }

    private fun bold(content: String) = "**$content**"

    private fun underlined(content: String) = "__${content}__"

    private fun link(url: String, text: String) = "[$text]($url)"

    private fun divider() {
        stringBuilder.append("\n---\n\n")
    }

    fun build() = stringBuilder.toString()

    fun clear() {
        stringBuilder.clear()
    }

    fun formatNews(news: News) {
        with(this) {
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

            divider()
        }
    }
}