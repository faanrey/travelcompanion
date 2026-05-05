package com.example.egypttravel.data.remote.api

import com.example.egypttravel.data.remote.dto.CityDetailJson
import com.example.egypttravel.data.remote.dto.ManifestJson
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Retrofit interface for the JSON content host (Cloudflare R2, Pages, or any
 * static file server).
 *
 * Two endpoints:
 *  - The manifest (items.json) — fixed path relative to the base URL.
 *  - A per-city detail file — passed as a full or relative URL, since the
 *    manifest tells us where each city's file lives via its `detailUrl` field.
 *
 * The `@Url` annotation lets us pass the URL at call time rather than baking
 * it into the interface. This keeps the API surface minimal and lets the
 * manifest control routing — adding a new city is a content-only change.
 */
interface CityApiService {

    /** Fetch the manifest listing all cities. */
    @GET("items.json")
    suspend fun fetchManifest(): ManifestJson

    /**
     * Fetch a per-city detail file. The [url] parameter is taken from
     * [com.example.egypttravel.data.remote.dto.ManifestCityJson.detailUrl]
     * — a relative path like "alexandria.json" or a full https URL.
     */
    @GET
    suspend fun fetchCityDetail(@Url url: String): CityDetailJson
}
