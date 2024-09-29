package org.example.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

object PlaceSerializer : KSerializer<String> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Place", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonElement = (decoder as JsonDecoder).decodeJsonElement()

        return when (jsonElement) {
            is JsonObject -> jsonElement["title"].toString().replace("\\R".toRegex(), " ")
            else -> "Неизвестно"
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value.toString())
    }
}