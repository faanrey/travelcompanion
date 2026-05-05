package com.example.egypttravel.data.mapper

import com.example.egypttravel.data.local.entity.PointOfInterestEntity
import com.example.egypttravel.data.remote.dto.ImageRefJson
import com.example.egypttravel.data.remote.dto.OpeningHoursJson
import com.example.egypttravel.data.remote.dto.PointOfInterestJson
import com.example.egypttravel.domain.model.Coordinates
import com.example.egypttravel.domain.model.ImageRef
import com.example.egypttravel.domain.model.LocalizedText
import com.example.egypttravel.domain.model.PoiCategory
import com.example.egypttravel.domain.model.PointOfInterest
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Mappers for [PointOfInterest] across the three model layers.
 *
 * JSON-blob fields (hours, images) require a Moshi instance to (de)serialize.
 * Rather than instantiating one per call, mappers receive shared Moshi
 * adapters from the PoiMappers class, which is constructed once via DI.
 *
 * This is the single place where JSON serialization happens at the entity
 * boundary; all other entity fields are plain columns.
 */
class PoiMappers(moshi: Moshi) {

    private val hoursAdapter: JsonAdapter<OpeningHoursJson> =
        moshi.adapter(OpeningHoursJson::class.java)

    private val imagesAdapter: JsonAdapter<List<ImageRefJson>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, ImageRefJson::class.java))

    // ----- JSON -> Entity -----

    fun jsonToEntity(json: PointOfInterestJson, cityId: String): PointOfInterestEntity =
        PointOfInterestEntity(
            id = json.id,
            cityId = cityId,
            category = json.category,
            nameEn = json.name.en,
            nameAr = json.name.ar,
            nameZhSimplified = json.name.zhSimplified,
            nameEsMx = json.name.esMx,
            descriptionEn = json.description.en,
            descriptionAr = json.description.ar,
            descriptionZhSimplified = json.description.zhSimplified,
            descriptionEsMx = json.description.esMx,
            addressEn = json.address.en,
            addressAr = json.address.ar,
            addressZhSimplified = json.address.zhSimplified,
            addressEsMx = json.address.esMx,
            latitude = json.coordinates.lat,
            longitude = json.coordinates.lng,
            hoursJson = json.hours?.let { hoursAdapter.toJson(it) },
            imagesJson = imagesAdapter.toJson(json.images),
            lastUpdated = System.currentTimeMillis()
        )

    // ----- Entity -> Domain -----

    fun entityToDomain(entity: PointOfInterestEntity): PointOfInterest = PointOfInterest(
        id = entity.id,
        cityId = entity.cityId,
        category = parseCategory(entity.category),
        name = LocalizedText(
            en = entity.nameEn,
            ar = entity.nameAr,
            zhSimplified = entity.nameZhSimplified,
            esMx = entity.nameEsMx
        ),
        description = LocalizedText(
            en = entity.descriptionEn,
            ar = entity.descriptionAr,
            zhSimplified = entity.descriptionZhSimplified,
            esMx = entity.descriptionEsMx
        ),
        address = LocalizedText(
            en = entity.addressEn,
            ar = entity.addressAr,
            zhSimplified = entity.addressZhSimplified,
            esMx = entity.addressEsMx
        ),
        coordinates = Coordinates(entity.latitude, entity.longitude),
        hours = entity.hoursJson
            ?.let { hoursAdapter.fromJson(it)?.toDomain() },
        images = (imagesAdapter.fromJson(entity.imagesJson) ?: emptyList())
            .map { it.toDomain() }
    )

    /**
     * Defensive enum parsing: an unknown category string falls back to
     * TOURIST_SITE rather than crashing. This protects users on older app
     * versions if the content team adds a new category before the app ships
     * support for it.
     */
    private fun parseCategory(raw: String): PoiCategory =
        runCatching { PoiCategory.valueOf(raw) }.getOrDefault(PoiCategory.TOURIST_SITE)
}

// Standalone DTO -> domain helper for ImageRef (no Moshi needed)
internal fun ImageRefJson.toDomain(): ImageRef = ImageRef(
    url = url,
    caption = caption?.toDomain(),
    attribution = attribution
)
