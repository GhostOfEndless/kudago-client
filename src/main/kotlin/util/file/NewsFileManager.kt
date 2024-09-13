package org.example.util.file

import org.example.dto.News

interface NewsFileManager {

    fun saveNews(path: String, news: Collection<News>)
}