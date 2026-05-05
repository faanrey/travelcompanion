package com.example.egypttravel.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Sync metadata kept outside the content tables.
 *
 * - `manifestVersion` is the last manifestVersion the app successfully ingested.
 *   On refresh, the repository compares the freshly fetched manifest's version
 *   to this; if equal, no individual cities need to be checked.
 * - `lastSyncEpochMillis` is the wall-clock time of the last completed sync.
 *   The UI may surface this as "last updated 3 days ago" when offline.
 *
 * Stored in DataStore (not Room) because this is app-level config metadata,
 * not content. Keeping it out of the database also avoids the need for a
 * single-row metadata table with the usual quirks.
 */
@Singleton
class SyncPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val manifestVersion: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_MANIFEST_VERSION] ?: 0
    }

    val lastSyncEpochMillis: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_SYNC] ?: 0L
    }

    suspend fun getManifestVersion(): Int = manifestVersion.first()

    suspend fun setManifestVersion(version: Int) {
        dataStore.edit { it[KEY_MANIFEST_VERSION] = version }
    }

    suspend fun setLastSyncNow() {
        dataStore.edit { it[KEY_LAST_SYNC] = System.currentTimeMillis() }
    }

    private companion object {
        val KEY_MANIFEST_VERSION = intPreferencesKey("manifest_version")
        val KEY_LAST_SYNC = longPreferencesKey("last_sync_epoch_millis")
    }
}
