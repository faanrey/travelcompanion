package com.example.egypttravel.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.egypttravel.data.local.dao.CityDao
import com.example.egypttravel.data.local.dao.PointOfInterestDao
import com.example.egypttravel.data.local.entity.CityEntity
import com.example.egypttravel.data.local.entity.PointOfInterestEntity

/**
 * Room database for the Egyptian travel companion app.
 *
 * No type converters are declared here: hours and images are stored as plain
 * String columns containing JSON, and (de)serialization happens in mappers
 * rather than at the Room boundary. This keeps the database layer dumb and
 * frees mappers to use the same Moshi instance used for network responses.
 *
 * Schema bumps:
 * - v1: initial — cities + points_of_interest with denormalized localized columns
 */
@Database(
    entities = [
        CityEntity::class,
        PointOfInterestEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class EgyptTravelDatabase : RoomDatabase() {

    abstract fun cityDao(): CityDao

    abstract fun pointOfInterestDao(): PointOfInterestDao

    companion object {
        const val DATABASE_NAME = "egypt_travel.db"
    }
}
