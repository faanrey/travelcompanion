package com.example.egypttravel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.egypttravel.data.local.entity.CityEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [CityEntity]. Read methods return [Flow] so the UI re-emits whenever
 * a refresh writes new data — this is the backbone of offline-first behavior:
 * the UI subscribes to the DB, and network refresh just updates rows.
 */
@Dao
interface CityDao {

    @Query("SELECT * FROM cities ORDER BY featured DESC, nameEn ASC")
    fun observeAll(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE id = :cityId LIMIT 1")
    fun observeById(cityId: String): Flow<CityEntity?>

    @Query("SELECT * FROM cities WHERE id = :cityId LIMIT 1")
    suspend fun getById(cityId: String): CityEntity?

    @Query("SELECT detailVersion FROM cities WHERE id = :cityId LIMIT 1")
    suspend fun getDetailVersion(cityId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(city: CityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(cities: List<CityEntity>)

    @Query("DELETE FROM cities WHERE id = :cityId")
    suspend fun deleteById(cityId: String)

    @Query("DELETE FROM cities WHERE id NOT IN (:keepIds)")
    suspend fun deleteCitiesNotIn(keepIds: List<String>)
}
