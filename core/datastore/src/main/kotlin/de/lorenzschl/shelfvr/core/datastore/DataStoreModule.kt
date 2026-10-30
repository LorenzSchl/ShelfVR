package de.lorenzschl.shelfvr.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/** DataStore für die verschlüsselten Tokens; getrennt von den Einstellungen. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenPreferences

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun settingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }

    @Provides
    @Singleton
    @TokenPreferences
    fun tokenDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("tokens") }

    @Provides
    @Singleton
    fun settingsDataSource(dataStore: DataStore<Preferences>): SettingsDataSource = SettingsDataSource(dataStore)
}
