package org.example.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.serializer.LocalDateTimeSerializer
import org.example.serializer.PlaceSerializer
import org.example.serializer.StringWithoutNewlinesSerializer
import kotlin.math.exp

@Serializable
data class News(
    val id: Int,
    @Serializable(with = StringWithoutNewlinesSerializer::class)
    val title: String,
    @Serializable(with = PlaceSerializer::class)
    val place: String,
    @Serializable(with = StringWithoutNewlinesSerializer::class)
    val description: String,
    @SerialName("site_url")
    @Serializable(with = StringWithoutNewlinesSerializer::class)
    val siteUrl: String,
    @SerialName("favorites_count")
    val favoritesCount: Int,
    @SerialName("comments_count")
    val commentsCount: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("publication_date")
    val publicationDate: LocalDateTime
) {
    val rating: Double
        get() = 1 / (1 + exp(-(favoritesCount.toDouble() / (commentsCount + 1))))
}
