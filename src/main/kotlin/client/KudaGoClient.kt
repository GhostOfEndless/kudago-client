package org.example.client

import org.example.dto.News

interface KudaGoClient {

    fun getNews(count: Int = 100): List<News>
}