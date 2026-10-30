package de.lorenzschl.shelfvr.core.network

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

internal const val HEADER_AUTHORIZATION = "Authorization"
internal const val HEADER_REFRESH_TOKEN = "x-refresh-token"

/**
 * Hängt den Access-Token an alle Requests an den angemeldeten Server – API und Audio-Streams
 * gleichermaßen, nie als Query-Parameter (PROJECT_SPEC.md §6.3).
 */
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val session = tokenStore.session.value
        if (session == null ||
            request.header(HEADER_AUTHORIZATION) != null ||
            !request.url.belongsTo(session.serverUrl) ||
            request.url.isUnauthenticatedEndpoint(session.serverUrl)
        ) {
            return chain.proceed(request)
        }
        return chain.proceed(
            request.newBuilder().header(HEADER_AUTHORIZATION, "Bearer ${session.accessToken}").build(),
        )
    }
}

internal fun HttpUrl.belongsTo(serverUrl: HttpUrl): Boolean =
    scheme == serverUrl.scheme && host == serverUrl.host && port == serverUrl.port &&
        encodedPath.startsWith(serverUrl.encodedPath)

private fun HttpUrl.isUnauthenticatedEndpoint(serverUrl: HttpUrl): Boolean {
    val relative = encodedPath.removePrefix(serverUrl.encodedPath)
    return relative == "login" || relative.startsWith("auth/")
}
