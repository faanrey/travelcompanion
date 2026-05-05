package com.example.egypttravel.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a point of interest within a city.
 *
 * Translatable fields (name, description, address) are denormalized into
 * per-language columns. Indices on the four `name<Lang>` columns speed up
 * `LIKE '%query%'` searches; the description and address columns are searched
 * less often and don't need indices.
 *
 * `hoursJson` and `imagesJson` are stored as serialized JSON because they are
 * never queried — only loaded for display. Type converters in [Converters]
 * handle the (de)serialization.
 *
 * Foreign key to [CityEntity] with CASCADE delete: removing a city removes
 * all its POIs in one operation.
 */
@Entity(
    tableName = "points_of_interest",
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["cityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("cityId"),
        Index("category"),
        Index("nameEn"),
        Index("nameAr"),
        Index("nameZhSimplified"),
        Index("nameEsMx")
    ]
)
data class PointOfInterestEntity(
    @PrimaryKey
    val id: String,

    val cityId: String,

    /** Stored as enum name string: TOURIST_SITE, FOOD, PARK, CHURCH, MOSQUE. */
    val category: String,

    // Localized name — denormalized, indexed
    val nameEn: String,
    val nameAr: String,
    val nameZhSimplified: String,
    val nameEsMx: String,

    // Localized description — denormalized, not indexed
    val descriptionEn: String,
    val descriptionAr: String,
    val descriptionZhSimplified: String,
    val descriptionEsMx: String,

    // Localized address — denormalized, not indexed
    val addressEn: String,
    val addressAr: String,
    val addressZhSimplified: String,
    val addressEsMx: String,

    val latitude: Double,
    val longitude: Double,

    /** Serialized OpeningHours object (or null if hours are unknown). */
    val hoursJson: String?,

    /** Serialized List<ImageRef> — empty array string "[]" if no images. */
    val imagesJson: String,

    val lastUpdated: Long
)
