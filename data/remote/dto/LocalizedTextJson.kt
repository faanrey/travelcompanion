package com.example.egypttravel.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * JSON wire format for a translatable string.
 * Mirrors the `{ "en": "...", "ar": "...", "zhSimplified": "...", "esMx": "..." }`
 * shape used in items.json and every per-city detail file.
 */
@JsonClass(generateAdapter = true)
data class LocalizedTextJson(
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String,
    @Json(name = "zhSimplified") val zhSimplified: String,
    @Json(name = "esMx") val esMx: String
)
