package com.example.egypttravel.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * JSON wire format for items.json — the manifest listing all available cities.
 *
 * `manifestVersion` is bumped whenever the manifest itself changes (a city is
 * added or removed, or any city's detailVersion is bumped). The app stores
 * the last-seen manifestVersion in [com.example.egypttravel.data.preferences.SyncPreferences];
 * if the fetched manifest's version is higher, we walk the cities list and
 * refetch any city whose detailVersion has increased.
 */
@JsonClass(generateAdapter = true)
data class ManifestJson(
    @Json(name = "manifestVersion") val manifestVersion: Int,
    @Json(name = "lastUpdated") val lastUpdated: String,
    @Json(name = "cities") val cities: List<ManifestCityJson>
)

@JsonClass(generateAdapter = true)
data class ManifestCityJson(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: LocalizedTextJson,
    @Json(name = "coordinates") val coordinates: CoordinatesJson,
    @Json(name = "detailVersion") val detailVersion: Int,
    @Json(name = "detailUrl") val detailUrl: String,
    @Json(name = "featured") val featured: Boolean = false
)
