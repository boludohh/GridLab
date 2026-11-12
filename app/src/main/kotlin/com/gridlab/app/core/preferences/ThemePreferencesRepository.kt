package com.gridlab.app.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gridlab.app.ui.theme.GridLabThemeMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ThemePreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val themeMode: Flow<GridLabThemeMode?> = dataStore.data.map { prefs ->
        when (prefs[KEY_THEME_MODE]) {
            VALUE_LIGHT -> GridLabThemeMode.Light
            VALUE_DARK -> GridLabThemeMode.Dark
            VALUE_NIGHT -> GridLabThemeMode.Night
            else -> null
        }
    }

    suspend fun setThemeMode(mode: GridLabThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = when (mode) {
                GridLabThemeMode.Light -> VALUE_LIGHT
                GridLabThemeMode.Dark -> VALUE_DARK
                GridLabThemeMode.Night -> VALUE_NIGHT
            }
        }
    }

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private const val VALUE_LIGHT = "light"
        private const val VALUE_DARK = "dark"
        private const val VALUE_NIGHT = "night"
    }
}
