package com.example.egypttravel.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * JSON wire format for a geographic coordinate pair.
 * Wire field names are `lat` and `lng` (shorter than `latitude`/`longitude`)
 * to keep JSON payloads small.
 */
@JsonClass(generateAdapter = true)
data class CoordinatesJson(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)
