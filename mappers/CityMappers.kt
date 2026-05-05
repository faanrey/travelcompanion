package com.example.egypttravel.data.mapper

import com.example.egypttravel.data.local.entity.CityEntity
import com.example.egypttravel.data.remote.dto.CityDetailJson
import com.example.egypttravel.data.remote.dto.ManifestCityJson
import com.example.egypttravel.domain.model.City
import com.example.egypttravel.domain.model.Coordinates
import com.example.egypttravel.domain.model.LocalizedText

/**
 * Mappers for [City] across the three model layers.
 *
 * Two JSON sources contribute to a [CityEntity]:
 *  - The manifest (items.json) supplies id, name, coordinates, featured flag,
 *    detailVersion. This is enough to populate the sidebar.
 *  - The per-city detail file supplies foundingDate, population, and the POI
 *    list. POIs are stored in their own table.
 *
 * The manifest mapper builds an entity with foundingDate=null and population=null
 * — these get filled in once the detail file is fetched. Because we use
 * INSERT OR REPLACE, the detail-file mapper produces a complete entity that
 * supersedes the manifest-only one.
 */

internal fun ManifestCityJson.toEntity(lastUpdated: Long): CityEntity = CityEntity(
    id = id,
    nameEn = name.en,
    nameAr = name.ar,
    nameZhSimplified = name.zhSimplified,
    nameEsMx = name.esMx,
    foundingDate = null,
    population = null,
    latitude = coordinates.lat,
    longitude = coordinates.lng,
    featured = featured,
    detailVersion = detailVersion,
    lastUpdated = lastUpdated
)

/**
 * Build a complete [CityEntity] by combining the manifest entry (for featured
 * flag and authoritative detailVersion from the manifest) with the detail
 * file's foundingDate/population. Both sources have the same id/name/coordinates;
 * we trust the manifest's view of the structural fields and the detail file's
 * view of the descriptive fields.
 */
internal fun CityDetailJson.toEntity(
    manifestEntry: ManifestCityJson,
    lastUpdated: Long
): CityEntity = CityEntity(
    id = id,
    nameEn = name.en,
    nameAr = name.ar,
    nameZhSimplified = name.zhSimplified,
    nameEsMx = name.esMx,
    foundingDate = foundingDate,
    population = population,
    latitude = coordinates.lat,
    longitude = coordinates.lng,
    featured = manifestEntry.featured,
    detailVersion = detailVersion,
    lastUpdated = lastUpdated
)

internal fun CityEntity.toDomain(): City = City(
    id = id,
    name = LocalizedText(
        en = nameEn,
        ar = nameAr,
        zhSimplified = nameZhSimplified,
        esMx = nameEsMx
    ),
    coordinates = Coordinates(latitude, longitude),
    foundingDate = foundingDate,
    population = population,
    featured = featured,
    // POIs are loaded separately via the POI DAO and joined at the domain level
    // by the use case or repository — keeping the entity-to-domain mapper pure.
    pointsOfInterest = emptyList()
)

/**
 * Build a domain [City] including its POIs. Used by the repository when the
 * caller wants the full picture in a single shot.
 */
internal fun CityEntity.toDomainWith(
    pois: List<com.example.egypttravel.domain.model.PointOfInterest>
): City = toDomain().copy(pointsOfInterest = pois)
