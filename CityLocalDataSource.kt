package com.example.egypttravel.data.local.datasource

import com.example.egypttravel.data.local.dao.CityDao
import com.example.egypttravel.data.local.dao.PointOfInterestDao
import com.example.egypttravel.data.local.entity.CityEntity
import com.example.egypttravel.data.local.entity.PointOfInterestEntity
import com.example.egypttravel.domain.model.Language
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Wraps the Room DAOs with a single, repository-friendly API. The repository
 * never touches DAOs directly — it goes through this data source so we can:
 * - Add caching/decoration logic in one place if needed later
 * - Coordinate writes that span multiple DAOs (e.g. upsert city + its POIs)
 * - Expose one method per logical operation rather than per-table method
 *
 * Search dispatches to the language-specific DAO method based on the [Language]
 * passed in. The repository observes the user's current language and re-invokes
 * search whenever it changes.
 */
@Singleton
class CityLocalDataSource @Inject constructor(
    private val cityDao: CityDao,
    private val poiDao: PointOfInterestDao
) {

    // ----- Cities -----

    fun observeCities(): Flow<List<CityEntity>> = cityDao.observeAll()

    fun observeCity(cityId: String): Flow<CityEntity?> = cityDao.observeById(cityId)

    suspend fun getCity(cityId: String): CityEntity? = cityDao.getById(cityId)

    suspend fun getCityDetailVersion(cityId: String): Int? =
        cityDao.getDetailVersion(cityId)

    suspend fun upsertCity(city: CityEntity) = cityDao.upsert(city)

    suspend fun upsertCities(cities: List<CityEntity>) = cityDao.upsertAll(cities)

    suspend fun deleteCitiesNotIn(keepIds: List<String>) =
        cityDao.deleteCitiesNotIn(keepIds)

    // ----- Points of interest -----

    fun observePointsOfInterest(cityId: String): Flow<List<PointOfInterestEntity>> =
        poiDao.observeByCity(cityId)

    fun observePointsOfInterestByCategory(
        cityId: String,
        category: String
    ): Flow<List<PointOfInterestEntity>> =
        poiDao.observeByCityAndCategory(cityId, category)

    fun observePointOfInterest(poiId: String): Flow<PointOfInterestEntity?> =
        poiDao.observeById(poiId)

    /**
     * Replace all POIs for a single city atomically. Use after a successful
     * fetch of that city's detail JSON.
     */
    suspend fun replacePointsOfInterestForCity(
        cityId: String,
        pois: List<PointOfInterestEntity>
    ) = poiDao.replaceForCity(cityId, pois)

    // ----- Search -----

    fun searchPointsOfInterest(
        query: String,
        language: Language
    ): Flow<List<PointOfInterestEntity>> = when (language) {
        Language.ENGLISH -> poiDao.searchInEnglish(query)
        Language.ARABIC -> poiDao.searchInArabic(query)
        Language.MANDARIN -> poiDao.searchInMandarin(query)
        Language.SPANISH -> poiDao.searchInSpanish(query)
    }
}
