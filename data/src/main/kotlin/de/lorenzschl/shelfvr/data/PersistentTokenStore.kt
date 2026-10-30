package de.lorenzschl.shelfvr.data

import android.os.Looper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.lorenzschl.shelfvr.core.network.AbsJson
import de.lorenzschl.shelfvr.core.network.AbsSession
import de.lorenzschl.shelfvr.core.network.TokenStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.Base64

/**
 * Sitzung (beide Tokens zusammen, damit die Rotation atomar gespeichert wird) verschlüsselt im
 * DataStore. Im MVP gibt es eine aktive Sitzung; ein Account-Wechsel erfordert neues Anmelden.
 */
class PersistentTokenStore(
    private val dataStore: DataStore<Preferences>,
    private val cipher: TokenCipher,
    private val scope: CoroutineScope,
) : TokenStore {

    private val state = MutableStateFlow<AbsSession?>(null)
    override val session: StateFlow<AbsSession?> = state
    private val restored = CompletableDeferred<Unit>()
    private val writeLock = Mutex()
    @Volatile private var updatedBeforeRestore = false

    init {
        scope.launch {
            val stored = runCatching { read() }.getOrNull()
            if (!updatedBeforeRestore) state.compareAndSet(null, stored)
            restored.complete(Unit)
        }
    }

    override suspend fun awaitRestored() = restored.await()

    /**
     * Abseits des Main-Threads (z. B. Token-Refresh in OkHttp) wird synchron geschrieben, damit ein
     * rotierter Refresh-Token einen Prozess-Abbruch übersteht. Auf dem Main-Thread asynchron.
     */
    override fun update(session: AbsSession?) {
        if (!restored.isCompleted) updatedBeforeRestore = true
        state.value = session
        if (Looper.myLooper() == Looper.getMainLooper()) {
            scope.launch { persistLatest() }
        } else {
            runBlocking { persistLatest() }
        }
    }

    private suspend fun persistLatest() = writeLock.withLock {
        val latest = state.value
        dataStore.edit { prefs ->
            if (latest == null) {
                prefs.remove(KEY)
            } else {
                val json = AbsJson.encodeToString(StoredSession.serializer(), StoredSession.from(latest))
                prefs[KEY] = Base64.getEncoder().encodeToString(cipher.encrypt(json.toByteArray()))
            }
        }
    }

    private suspend fun read(): AbsSession? {
        val encoded = dataStore.data.first()[KEY] ?: return null
        val json = try {
            String(cipher.decrypt(Base64.getDecoder().decode(encoded)))
        } catch (_: Exception) {
            // Schlüssel verloren (z. B. Neuinstallation) oder Daten beschädigt: neu anmelden.
            dataStore.edit { it.remove(KEY) }
            return null
        }
        return AbsJson.decodeFromString(StoredSession.serializer(), json).toSession()
    }

    @Serializable
    private data class StoredSession(
        val serverUrl: String,
        val username: String,
        val accessToken: String,
        val refreshToken: String,
    ) {
        fun toSession(): AbsSession? =
            serverUrl.toHttpUrlOrNull()?.let { AbsSession(it, username, accessToken, refreshToken) }

        override fun toString() = "StoredSession(serverUrl=$serverUrl, username=$username)"

        companion object {
            fun from(s: AbsSession) = StoredSession(s.serverUrl.toString(), s.username, s.accessToken, s.refreshToken)
        }
    }

    private companion object {
        val KEY = stringPreferencesKey("session")
    }
}
