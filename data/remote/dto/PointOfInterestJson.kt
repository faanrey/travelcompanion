package com.example.egypttravel.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * JSON wire format for a single point of interest within a city.
 *
 * `category` is a string matching the names in PoiCategory:
 * TOURIST_SITE, FOOD, PARK, CHURCH, MOSQUE. Mappers convert it to the enum.
 */
@JsonClass(generateAdapter = true)
data class PointOfInterestJson(
    @Json(name = "id") val id: String,
    @Json(name = "category") val category: String,
    @Json(name = "name") val name: LocalizedTextJson,
    @Json(name = "description") val description: LocalizedTextJson,
    @Json(name = "coordinates") val coordinates: CoordinatesJson,
    @Json(name = "hours") val hours: OpeningHoursJson? = null,
    @Json(name = "address") val address: LocalizedTextJson,
    @Json(name = "images") val images: List<ImageRefJson> = emptyList()
)
