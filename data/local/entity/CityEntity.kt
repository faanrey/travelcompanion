package com.example.egypttravel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a city.
 *
 * LocalizedText fields (name) are denormalized into per-language columns to:
 * - Allow fast indexed LIKE queries for search in any language
 * - Avoid runtime JSON parsing on every read
 * - Stay within SQLite's standard query syntax (no json_extract)
 *
 * `detailVersion` tracks which version of the city's JSON file we currently
 * hold, so the repository can compare against the manifest and skip refresh
 * if nothing changed.
 */
@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey
    val id: String,

    // Localized name — denormalized
    val nameEn: String,
    val nameAr: String,
    val nameZhSimplified: String,
    val nameEsMx: String,

    val foundingDate: String?,
    val population: Int?,

    val latitude: Double,
    val longitude: Double,

    val featured: Boolean,

    /** Version of the city's detail JSON file currently cached. */
    val detailVersion: Int,

    /** Epoch millis of last successful refresh of this city. */
    val lastUpdated: Long
)
