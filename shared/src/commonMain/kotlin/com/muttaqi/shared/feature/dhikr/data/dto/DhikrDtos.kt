package com.muttaqi.shared.feature.dhikr.data.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal typealias Translations = Map<String, String>

@Serializable
internal data class DhikrBookDto(val sections: List<Section>) {
    @Serializable
    data class Section(val id: String, val title: String, val subtitle: String, val dhikr: List<Entry>)

    @Serializable
    data class Entry(
        val id: String,
        val title: String? = null,
        val arabic: String,
        val transliteration: String? = null,
        val translation: Translations? = null,
        val steps: List<Step>? = null,
        val count: Int? = null,
        val hadith: Translations? = null,
        val reference: String,
        val grade: String,
        val credit: Credit? = null,
    )

    @Serializable
    data class Credit(val translation: Map<String, List<String>>? = null, val hadith: Map<String, List<String>>? = null)

    @Serializable
    data class Step(val arabic: String, val transliteration: String? = null, val translation: Translations, val count: Int)
}

@Serializable
internal data class DhikrProgressDto(
    val count: Int,
    val rounds: Int,
    @Serializable(with = WholeSecondsSerializer::class) val day: Double,
)

internal object WholeSecondsSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("WholeSeconds", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Double = decoder.decodeDouble()

    override fun serialize(encoder: Encoder, value: Double) {
        val whole = value.toLong()
        if (whole.toDouble() == value) encoder.encodeLong(whole) else encoder.encodeDouble(value)
    }
}
