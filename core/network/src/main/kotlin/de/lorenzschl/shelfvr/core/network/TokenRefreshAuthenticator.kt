package de.lorenzschl.shelfvr.core.network

import de.lorenzschl.shelfvr.core.network.dto.LoginResponse
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.io.IOException

/**
 * Bei 401 einmalig `POST /auth/refresh` aufrufen, beide rotierten Tokens speichern und den
 * Request wiederholen. Parallele 401er teilen sich einen Refresh: Wer den Lock bekommt, nachdem
 * ein anderer Thread schon erneuert hat, nimmt einfach den neuen Token (PROJECT_SPEC.md §6.1).
 *
 * OkHttp ruft Authenticatoren blockierend auf, deshalb `synchronized` statt Coroutine-Mutex.
 * Die Instanz muss prozessweit eindeutig sein.
 */
class TokenRefreshAuthenticator(
    private val tokenStore: TokenStore,
    private val refresher: TokenRefresher,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        val usedToken = request.header(HEADER_AUTHORIZATION)?.removePrefix("Bearer ") ?: return null
        if (response.priorResponse != null) return null // Wiederholung ist auch gescheitert.

        val session = synchronized(lock) {
            val current = tokenStore.session.value ?: return null
            if (!request.url.belongsTo(current.serverUrl)) return null
            if (current.accessToken != usedToken) {
                current
            } else {
                val refreshed = refresher.refresh(current)
                tokenStore.update(refreshed)
                refreshed ?: return null
            }
        }
        return request.newBuilder().header(HEADER_AUTHORIZATION, "Bearer ${session.accessToken}").build()
    }
}

/** Führt den eigentlichen Refresh-Call aus; liefert `null`, wenn der Refresh-Token ungültig ist. */
class TokenRefresher(private val client: OkHttpClient, private val json: Json = AbsJson) {

    fun refresh(session: AbsSession): AbsSession? {
        val request = Request.Builder()
            .url(session.serverUrl.resolveServerPath("auth/refresh"))
            .header(HEADER_REFRESH_TOKEN, session.refreshToken)
            .post(ByteArray(0).toRequestBody())
            .build()
        val user = try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                json.decodeFromString<LoginResponse>(response.body.string()).user
            }
        } catch (_: IOException) {
            // Netzfehler: Sitzung nicht verwerfen, nur diesen Request scheitern lassen.
            return session
        }
        val access = user.accessToken ?: return null
        val refresh = user.refreshToken ?: return null
        return session.copy(accessToken = access, refreshToken = refresh)
    }
}
