package de.lorenzschl.shelfvr.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lorenzschl.shelfvr.core.network.AbsClientFactory
import de.lorenzschl.shelfvr.core.network.CleartextPolicy
import de.lorenzschl.shelfvr.core.network.InMemoryCleartextPolicy
import de.lorenzschl.shelfvr.core.network.InMemoryTokenStore
import de.lorenzschl.shelfvr.core.network.TokenStore
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Prozessweit eindeutig: alle Fenster und der PlaybackService teilen Tokens und Refresh-Lock.
    // InMemory nur bis FR-02 (verschlüsselter Speicher, M1).
    @Provides
    @Singleton
    fun tokenStore(): TokenStore = InMemoryTokenStore()

    // HTTP-Zustimmungen pro Server (§3.2); InMemory nur bis zur Persistenz (#5).
    @Provides
    @Singleton
    fun cleartextPolicy(): CleartextPolicy = InMemoryCleartextPolicy()

    @Provides
    @Singleton
    fun httpClient(tokenStore: TokenStore, cleartextPolicy: CleartextPolicy): OkHttpClient =
        AbsClientFactory.createHttpClient(tokenStore, cleartextPolicy)
}
