package de.lorenzschl.shelfvr.core.network

import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Defensiv deserialisieren: ABS ergänzt laufend Felder (PROJECT_SPEC.md RI-06). */
val AbsJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

object AbsClientFactory {

    /**
     * Die eine OkHttp-Instanz für API und Audio-Streaming (Media3 `OkHttpDataSource`).
     * Kein HTTP-Logging: Header enthalten Tokens. Der Refresh-Client erbt die Cleartext-Regel.
     */
    fun createHttpClient(
        tokenStore: TokenStore,
        cleartextPolicy: CleartextPolicy,
        base: OkHttpClient = OkHttpClient(),
    ): OkHttpClient {
        val cleartextGuard = CleartextGuardInterceptor(cleartextPolicy)
        val plain = base.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            // Nie automatisch zwischen HTTPS und HTTP wechseln (§3.2).
            .followSslRedirects(false)
            .addInterceptor(cleartextGuard)
            .addNetworkInterceptor(cleartextGuard)
            .build()
        return plain.newBuilder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .authenticator(TokenRefreshAuthenticator(tokenStore, TokenRefresher(plain)))
            .build()
    }

    fun createApi(serverUrl: HttpUrl, client: OkHttpClient): AbsApi =
        Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(client)
            .addConverterFactory(AbsJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AbsApi::class.java)
}
