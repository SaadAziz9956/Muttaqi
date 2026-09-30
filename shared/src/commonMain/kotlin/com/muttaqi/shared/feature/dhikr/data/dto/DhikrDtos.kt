package com.muttaqi.shared.feature.dhikr.data.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Published translations by language code, e.g. {"en": …, "ur": …} */
internal typealias Translations = Map<String, String>

/** Dhikr.json: general remembrance from the Quran and the hadith collections, grouped into sections */
@Serializable
internal data class DhikrBookDto(val sections: List<Section>) {
    @Serializable
    data class Section(val id: String, val title: String, val subtitle: String, val dhikr: List<Entry>)

    @Serializable
    data class Entry(
        val id: String,
        val title: String? = null,
        val arabic: String,
        val transliteration: String,
        val translation: Translations? = null,
        val steps: List<Step>? = null,
        val count: Int? = null,
        val hadith: Translations? = null,
        val reference: String,
        val grade: String,
        val credit: Translations? = null,
    )

    @Serializable
    data class Step(val arabic: String, val transliteration: String, val translation: Translations, val count: Int)
}

/**
 * One dhikr's progress as the Swift app has always stored it: Foundation's JSONEncoder output, with the day as
 * seconds since 2001-01-01 UTC (JSONEncoder's default for a Date), e.g. {"count":7,"rounds":0,"day":812401200}
 */
@Serializable
internal data class DhikrProgressDto(
    val count: Int,
    val rounds: Int,
    @Serializable(with = WholeSecondsSerializer::class) val day: Double,
)

/** Reads any JSON number, and writes whole seconds without a fraction or exponent, as JSONEncoder does */
internal object WholeSecondsSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("WholeSeconds", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Double = decoder.decodeDouble()

    override fun serialize(encoder: Encoder, value: Double) {
        val whole = value.toLong()
        if (whole.toDouble() == value) encoder.encodeLong(whole) else encoder.encodeDouble(value)
    }
}
