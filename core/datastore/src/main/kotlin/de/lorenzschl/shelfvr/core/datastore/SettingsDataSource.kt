package de.lorenzschl.shelfvr.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import de.lorenzschl.shelfvr.core.model.AppSettings
import de.lorenzschl.shelfvr.core.model.SortKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Einstellungen und HTTP-Zustimmungen als Preferences-DataStore; fensterübergreifend als Flow (§5.1). */
class SettingsDataSource(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<AppSettings> = dataStore.data.map { it.toSettings() }.distinctUntilChanged()

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    /** Ursprünge (Schema+Host+Port), für die HTTP freigegeben ist (§3.2). */
    val cleartextOrigins: Flow<Set<String>> =
        dataStore.data.map { it[Keys.cleartextOrigins].orEmpty() }.distinctUntilChanged()

    suspend fun setCleartextAllowed(origin: String, allowed: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.cleartextOrigins].orEmpty()
            prefs[Keys.cleartextOrigins] = if (allowed) current + origin else current - origin
        }
    }

    private fun Preferences.toSettings() = AppSettings(
        activeLibraryId = this[Keys.activeLibraryId],
        sortKey = this[Keys.sortKey]?.let { name -> SortKey.entries.firstOrNull { it.name == name } } ?: SortKey.TITLE,
        sortDesc = this[Keys.sortDesc] ?: false,
        skipBackSeconds = this[Keys.skipBack] ?: 15,
        skipForwardSeconds = this[Keys.skipForward] ?: 30,
        defaultSpeed = this[Keys.defaultSpeed] ?: 1.0f,
        syncIntervalSeconds = this[Keys.syncInterval] ?: 15,
        pauseOnHeadsetRemoved = this[Keys.pauseOnHeadsetRemoved] ?: false,
    )

    private fun MutablePreferences.write(settings: AppSettings) {
        settings.activeLibraryId?.let { this[Keys.activeLibraryId] = it } ?: remove(Keys.activeLibraryId)
        this[Keys.sortKey] = settings.sortKey.name
        this[Keys.sortDesc] = settings.sortDesc
        this[Keys.skipBack] = settings.skipBackSeconds
        this[Keys.skipForward] = settings.skipForwardSeconds
        this[Keys.defaultSpeed] = settings.defaultSpeed
        this[Keys.syncInterval] = settings.syncIntervalSeconds
        this[Keys.pauseOnHeadsetRemoved] = settings.pauseOnHeadsetRemoved
    }

    private object Keys {
        val activeLibraryId = stringPreferencesKey("active_library_id")
        val sortKey = stringPreferencesKey("sort_key")
        val sortDesc = booleanPreferencesKey("sort_desc")
        val skipBack = intPreferencesKey("skip_back_seconds")
        val skipForward = intPreferencesKey("skip_forward_seconds")
        val defaultSpeed = floatPreferencesKey("default_speed")
        val syncInterval = intPreferencesKey("sync_interval_seconds")
        val pauseOnHeadsetRemoved = booleanPreferencesKey("pause_on_headset_removed")
        val cleartextOrigins = stringSetPreferencesKey("cleartext_origins")
    }
}
