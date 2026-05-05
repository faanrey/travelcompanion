package com.example.egypttravel.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * JSON wire format for an image reference attached to a POI.
 *
 * `url` may be a remote URL (https://...) or a bundled-asset URI
 * (asset:///alexandria/library-1.jpg). Coil handles both transparently.
 */
@JsonClass(generateAdapter = true)
data class ImageRefJson(
    @Json(name = "url") val url: String,
    @Json(name = "caption") val caption: LocalizedTextJson? = null,
    @Json(name = "attribution") val attribution: String? = null
)
