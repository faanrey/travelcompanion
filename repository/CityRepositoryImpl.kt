package com.example.egypttravel.data.repository

import com.example.egypttravel.data.local.datasource.CityLocalDataSource
import com.example.egypttravel.data.mapper.PoiMappers
import com.example.egypttravel.data.mapper.toDomain
import com.example.egypttravel.data.mapper.toDomainWith
import com.example.egypttravel.data.mapper.toEntity
import com.example.egypttravel.data.preferences.LanguagePreferences
import com.example.egypttravel.data.preferences.SyncPreferences
import com.example.egypttravel.data.remote.datasource.CityRemoteDataSource
import com.example.egypttravel.data.remote.dto.ManifestCityJson
import com.example.egypttravel.domain.exception.CityException
import com.example.egypttravel.domain.model.City
import com.example.egypttravel.domain.model.PoiCategory
import com.example.egypttravel.domain.model.PointOfInterest
import com.example.egypttravel.domain.repository.CityRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Offline-first implementation of [CityRepository].
 *
 * Reads always observe Room — the UI never blocks on the network. Writes are
 * triggered explicitly via [refresh], which fetches the manifest, compares
 * versions, and updates only the cities whose `detailVersion` has actually
 * changed. Failures during refresh do not propagate to UI reads; callers
 * who care about success/failure of a refresh attempt observe the result of
 * [refresh] directly.
 *
 * Refresh strategy:
 *  1. Fetch items.json.
 *  2. If its `manifestVersion` <= our stored manifestVersion, exit early.
 *  3. Otherwise, for each city in the manifest, compare its `detailVersion`
 *     against our stored detailVersion for that city's row. Fetch only the
 *     changed cities.
 *  4. Delete cities the manifest no longer lists.
 *  5. Persist the new manifestVersion and last-sync timestamp.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CityRepositoryImpl @Inject constructor(
    private val local: CityLocalDataSource,
    private val remote: CityRemoteDataSource,
    private val syncPreferences: SyncPreferences,
    private val languagePreferences: LanguagePreferences,
    private val poiMappers: PoiMappers
) : CityRepository {

    // ----- Reads (offline-first, always Room-backed) -----

    override fun observeCities(): Flow<List<City>> =
        local.observeCities().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeCity(cityId: String): Flow<City?> =
        combine(
            local.observeCity(cityId),
            local.observePointsOfInterest(cityId)
        ) { entity, poiEntities ->
            entity?.toDomainWith(
                pois = poiEntities.map(poiMappers::entityToDomain)
            )
        }

    override fun observePointsOfInterest(cityId: String): Flow<List<PointOfInterest>> =
        local.observePointsOfInterest(cityId).map { entities ->
            entities.map(poiMappers::entityToDomain)
        }

    override fun observePointsOfInterestByCategory(
        cityId: String,
        category: PoiCategory
    ): Flow<List<PointOfInterest>> =
        local.observePointsOfInterestByCategory(cityId, category.name)
            .map { entities -> entities.map(poiMappers::entityToDomain) }

    /**
     * Search re-runs whenever either the query OR the user's language changes.
     * `flatMapLatest` cancels the previous DAO subscription when the inputs
     * change so we don't leak collectors.
     */
    override fun searchPointsOfInterest(query: String): Flow<List<PointOfInterest>> {
        if (query.isBlank()) return flowOf(emptyList())
        return languagePreferences.currentLanguage.flatMapLatest { language ->
            local.searchPointsOfInterest(query, language)
                .map { entities -> entities.map(poiMappers::entityToDomain) }
        }
    }

    // ----- Refresh -----

    override suspend fun refresh(): Result<Unit> = runCatching {
        val manifest = remote.fetchManifest()
        val storedManifestVersion = syncPreferences.getManifestVersion()

        if (manifest.manifestVersion <= storedManifestVersion) {
            // Nothing to do — content is up to date.
            syncPreferences.setLastSyncNow()
            return@runCatching
        }

        // Persist the manifest entries first so the sidebar reflects any new
        // cities even before their detail files arrive. This is the key
        // offline-first move: surface what we know, fill in the rest.
        val now = System.currentTimeMillis()
        val manifestEntities = manifest.cities.map { it.toEntity(now) }
        local.upsertCities(manifestEntities)

        // Drop cities the manifest no longer lists.
        local.deleteCitiesNotIn(manifest.cities.map { it.id })

        // Refetch each city whose detailVersion has bumped past what we have.
        manifest.cities.forEach { manifestEntry ->
            val storedVersion = local.getCityDetailVersion(manifestEntry.id) ?: 0
            if (manifestEntry.detailVersion > storedVersion ||
                local.getCity(manifestEntry.id)?.foundingDate == null
            ) {
                refreshSingleCity(manifestEntry, now)
            }
        }

        syncPreferences.setManifestVersion(manifest.manifestVersion)
        syncPreferences.setLastSyncNow()
    }.recoverCatching { throwable ->
        // Translate any non-domain exception into a domain one before re-throw.
        throw when (throwable) {
            is CityException -> throwable
            else -> CityException.ParseError(throwable.message ?: "Unknown refresh error")
        }
    }

    /**
     * Fetch one city's detail file and persist its data atomically.
     * Called from [refresh]; surfaced as a separate function so it's easy
     * to test the per-city refresh in isolation.
     */
    private suspend fun refreshSingleCity(
        manifestEntry: ManifestCityJson,
        timestamp: Long
    ) {
        val detail = remote.fetchCityDetail(manifestEntry.detailUrl)

        // Defensive check: if the detail file's id doesn't match the manifest
        // entry, something is wrong on the server. Skip rather than corrupting
        // local data with a mismatched record.
        if (detail.id != manifestEntry.id) return

        val cityEntity = detail.toEntity(manifestEntry, timestamp)
        local.upsertCity(cityEntity)

        val poiEntities = detail.pointsOfInterest.map { poiJson ->
            poiMappers.jsonToEntity(poiJson, cityId = detail.id)
        }
        local.replacePointsOfInterestForCity(detail.id, poiEntities)
    }
}
