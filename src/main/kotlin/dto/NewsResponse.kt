package org.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<News>
)
