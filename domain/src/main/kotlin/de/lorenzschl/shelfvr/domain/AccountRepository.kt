package de.lorenzschl.shelfvr.domain

import de.lorenzschl.shelfvr.core.model.Account
import de.lorenzschl.shelfvr.core.model.Server
import kotlinx.coroutines.flow.Flow

data class ActiveAccount(val server: Server, val account: Account)

/** Server und Accounts (FR-05). Tokens verwaltet getrennt der TokenStore. */
interface AccountRepository {

    /** Der aktive Account; bleibt auch nach gescheitertem Refresh bestehen (Vorbelegung, FR-03). */
    val activeAccount: Flow<ActiveAccount?>

    /** Nach erfolgreichem Login: Server und Account anlegen bzw. aktualisieren und aktiv setzen. */
    suspend fun saveLogin(
        serverUrl: String,
        serverVersion: String?,
        userId: String,
        username: String,
        defaultLibraryId: String?,
    ): ActiveAccount

    /** Abmelden (FR-04): kein Account mehr aktiv. */
    suspend fun signOut()
}
