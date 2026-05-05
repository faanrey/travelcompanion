package com.example.egypttravel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.egypttravel.data.local.entity.PointOfInterestEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [PointOfInterestEntity].
 *
 * Search is split across four `searchInXxx` methods rather than one parameterized
 * query because Room does not allow column names to be passed as parameters —
 * the language must be baked into the SQL at compile time. This is also faster:
 * each query can use the index on its specific name column.
 *
 * The repository chooses which method to invoke based on the user's current
 * language preference.
 */
@Dao
interface PointOfInterestDao {

    @Query("SELECT * FROM points_of_interest WHERE cityId = :cityId ORDER BY nameEn ASC")
    fun observeByCity(cityId: String): Flow<List<PointOfInterestEntity>>

    @Query(
        """
        SELECT * FROM points_of_interest 
        WHERE cityId = :cityId AND category = :category 
        ORDER BY nameEn ASC
        """
    )
    fun observeByCityAndCategory(cityId: String, category: String): Flow<List<PointOfInterestEntity>>

    @Query("SELECT * FROM points_of_interest WHERE id = :poiId LIMIT 1")
    fun observeById(poiId: String): Flow<PointOfInterestEntity?>

    // ----- Language-specific search methods -----
    // Each searches name + description + address for the given language.
    // The leading/trailing '%' are added in SQL via ||, so callers pass a raw query string.

    @Query(
        """
        SELECT * FROM points_of_interest 
        WHERE nameEn LIKE '%' || :query || '%'
           OR descriptionEn LIKE '%' || :query || '%'
           OR addressEn LIKE '%' || :query || '%'
        ORDER BY nameEn ASC
        """
    )
    fun searchInEnglish(query: String): Flow<List<PointOfInterestEntity>>

    @Query(
        """
        SELECT * FROM points_of_interest 
        WHERE nameAr LIKE '%' || :query || '%'
           OR descriptionAr LIKE '%' || :query || '%'
           OR addressAr LIKE '%' || :query || '%'
        ORDER BY nameAr ASC
        """
    )
    fun searchInArabic(query: String): Flow<List<PointOfInterestEntity>>

    @Query(
        """
        SELECT * FROM points_of_interest 
        WHERE nameZhSimplified LIKE '%' || :query || '%'
           OR descriptionZhSimplified LIKE '%' || :query || '%'
           OR addressZhSimplified LIKE '%' || :query || '%'
        ORDER BY nameZhSimplified ASC
        """
    )
    fun searchInMandarin(query: String): Flow<List<PointOfInterestEntity>>

    @Query(
        """
        SELECT * FROM points_of_interest 
        WHERE nameEsMx LIKE '%' || :query || '%'
           OR descriptionEsMx LIKE '%' || :query || '%'
           OR addressEsMx LIKE '%' || :query || '%'
        ORDER BY nameEsMx ASC
        """
    )
    fun searchInSpanish(query: String): Flow<List<PointOfInterestEntity>>

    // ----- Writes -----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(pois: List<PointOfInterestEntity>)

    @Query("DELETE FROM points_of_interest WHERE cityId = :cityId")
    suspend fun deleteByCity(cityId: String)

    /**
     * Atomically replaces all POIs for a city. Used when a city's detail JSON
     * has been refetched — we wipe the old POIs and insert the new ones in one
     * transaction so the UI never sees an empty intermediate state.
     */
    @Transaction
    suspend fun replaceForCity(cityId: String, newPois: List<PointOfInterestEntity>) {
        deleteByCity(cityId)
        upsertAll(newPois)
    }
}
