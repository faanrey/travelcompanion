package com.example.egypttravel.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * JSON wire format for a per-city detail file (e.g. alexandria.json).
 * Returned by [com.example.egypttravel.data.remote.api.CityApiService.fetchCityDetail].
 *
 * `detailVersion` here must match the version listed for this city in
 * items.json — the manifest. The repository uses this to validate that the
 * detail file is in sync with the manifest before persisting.
 */
@JsonClass(generateAdapter = true)
data class CityDetailJson(
    @Json(name = "id") val id: String,
    @Json(name = "detailVersion") val detailVersion: Int,
    @Json(name = "name") val name: LocalizedTextJson,
    @Json(name = "coordinates") val coordinates: CoordinatesJson,
    @Json(name = "foundingDate") val foundingDate: String? = null,
    @Json(name = "population") val population: Int? = null,
    @Json(name = "pointsOfInterest") val pointsOfInterest: List<PointOfInterestJson>
)
