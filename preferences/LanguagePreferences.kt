package com.example.egypttravel.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.egypttravel.domain.model.Language
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Observable preference for the user's current language.
 *
 * The repository's search method observes this and re-runs whenever it changes,
 * so swapping the app language instantly re-queries POIs against the matching
 * language column.
 *
 * Default is [Language.ENGLISH]; the UI's language picker calls [setLanguage]
 * to change it, which also triggers the AppCompatDelegate locale change in
 * the presentation layer.
 */
@Singleton
class LanguagePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val currentLanguage: Flow<Language> = dataStore.data.map { prefs ->
        val code = prefs[KEY_LANGUAGE_CODE]
        Language.fromCode(code) ?: Language.ENGLISH
    }

    suspend fun setLanguage(language: Language) {
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE_CODE] = language.code
        }
    }

    private companion object {
        val KEY_LANGUAGE_CODE = stringPreferencesKey("language_code")
    }
}
