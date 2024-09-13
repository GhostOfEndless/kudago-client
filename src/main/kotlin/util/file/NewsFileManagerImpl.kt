package org.example.util.file

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.dto.News
import java.io.File

class NewsFileManagerImpl: NewsFileManager {

    private val logger = KotlinLogging.logger {}

    override fun saveNews(path: String, news: Collection<News>) {
        val file = File(path)
        if (file.exists()) {
            logger.warn { "File already exists at path: $path File will be rewrite" }
            file.delete()
        }

        file.bufferedWriter().use { writer ->
            writer.write("id;title;place;description;site_url;favorites_count;comments_count;publication_date;rating\n")
            news.forEach { newsItem ->
                writer.write("${newsItem.id};${newsItem.title};${newsItem.place};${newsItem.description};" +
                        "${newsItem.siteUrl};${newsItem.favoritesCount};${newsItem.commentsCount};" +
                        "${newsItem.publicationDate};${newsItem.rating}\n")
            }
        }
    }
}