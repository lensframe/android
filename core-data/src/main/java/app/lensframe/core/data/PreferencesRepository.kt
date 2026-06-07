package app.lensframe.core.data

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit

@Singleton
class PreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private object PreferencesKeys {
        val SELECTED_FOLDERS = stringSetPreferencesKey("selected_folders")
        val ROTATION_ENABLED = booleanPreferencesKey("rotation_enabled")
    }

    val selectedFoldersFlow: Flow<List<Uri>> = dataStore.data.map { preferences ->
        val stringSet = preferences[PreferencesKeys.SELECTED_FOLDERS] ?: emptySet()
        stringSet.map { it.toUri() }
    }

    val isRotationEnabledFlow: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[PreferencesKeys.ROTATION_ENABLED] ?: true }

    suspend fun addFolder(uri: Uri) {
        dataStore.edit { preferences ->
            val currentSet = preferences[PreferencesKeys.SELECTED_FOLDERS] ?: emptySet()
            preferences[PreferencesKeys.SELECTED_FOLDERS] = currentSet + uri.toString()
        }
    }

    suspend fun removeFolder(uri: Uri) {
        dataStore.edit { preferences ->
            val currentSet = preferences[PreferencesKeys.SELECTED_FOLDERS] ?: emptySet()
            preferences[PreferencesKeys.SELECTED_FOLDERS] = currentSet - uri.toString()
        }
    }

    suspend fun setRotationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ROTATION_ENABLED] = enabled
        }
    }
}