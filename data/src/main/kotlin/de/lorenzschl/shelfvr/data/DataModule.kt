package de.lorenzschl.shelfvr.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lorenzschl.shelfvr.core.datastore.SettingsDataSource
import de.lorenzschl.shelfvr.core.datastore.TokenPreferences
import de.lorenzschl.shelfvr.core.network.CleartextPolicy
import de.lorenzschl.shelfvr.core.network.TokenStore
import de.lorenzschl.shelfvr.domain.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/** Prozessweiter Scope für Hintergrundarbeit, die einzelne Fenster überlebt. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Prozessweit eindeutig: alle Fenster und der PlaybackService teilen Tokens und Refresh-Lock (§6.1).
    @Provides
    @Singleton
    fun tokenStore(
        @TokenPreferences dataStore: DataStore<Preferences>,
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
    ): TokenStore = PersistentTokenStore(dataStore, keystoreTokenCipher(context), scope)

    @Provides
    @Singleton
    fun cleartextPolicy(settings: SettingsDataSource, @ApplicationScope scope: CoroutineScope): CleartextPolicy =
        PersistentCleartextPolicy(settings, scope)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindings {
    @Binds
    abstract fun accountRepository(impl: RoomAccountRepository): AccountRepository
}
