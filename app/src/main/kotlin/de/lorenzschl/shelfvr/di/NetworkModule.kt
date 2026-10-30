package de.lorenzschl.shelfvr.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lorenzschl.shelfvr.core.network.AbsClientFactory
import de.lorenzschl.shelfvr.core.network.CleartextPolicy
import de.lorenzschl.shelfvr.core.network.TokenStore
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // TokenStore und CleartextPolicy kommen persistent aus :data (DataModule).
    @Provides
    @Singleton
    fun httpClient(tokenStore: TokenStore, cleartextPolicy: CleartextPolicy): OkHttpClient =
        AbsClientFactory.createHttpClient(tokenStore, cleartextPolicy)
}
