package de.lorenzschl.shelfvr.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.HttpUrl

/** Angemeldete Sitzung an einem ABS-Server. */
data class AbsSession(
    val serverUrl: HttpUrl,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
) {
    // Tokens nie in Logs oder Fehlermeldungen (PROJECT_SPEC.md §0 Regel 6).
    override fun toString() = "AbsSession(serverUrl=$serverUrl, username=$username)"
}

/**
 * Prozessweiter Speicher der aktuellen Sitzung. Muss im DI-Graph ein Singleton sein, damit alle
 * Fenster denselben Token-Stand sehen (PROJECT_SPEC.md §6.1 Punkt 3).
 */
interface TokenStore {
    val session: StateFlow<AbsSession?>
    fun update(session: AbsSession?)

    /** Wartet, bis eine gespeicherte Sitzung geladen ist (nach App-Start oder Fenster-Wiederherstellung). */
    suspend fun awaitRestored() {}
}

/** Nur für Spikes und Tests; die verschlüsselte Variante folgt mit FR-02 (M1). */
class InMemoryTokenStore(initial: AbsSession? = null) : TokenStore {
    private val state = MutableStateFlow(initial)
    override val session: StateFlow<AbsSession?> = state
    override fun update(session: AbsSession?) {
        state.value = session
    }
}
