package de.lorenzschl.shelfvr.data

import de.lorenzschl.shelfvr.core.database.AccountDao
import de.lorenzschl.shelfvr.core.database.AccountEntity
import de.lorenzschl.shelfvr.core.database.ServerDao
import de.lorenzschl.shelfvr.core.database.ServerEntity
import de.lorenzschl.shelfvr.core.model.Account
import de.lorenzschl.shelfvr.core.model.Server
import de.lorenzschl.shelfvr.domain.AccountRepository
import de.lorenzschl.shelfvr.domain.ActiveAccount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class RoomAccountRepository @Inject constructor(
    private val serverDao: ServerDao,
    private val accountDao: AccountDao,
) : AccountRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val activeAccount: Flow<ActiveAccount?> = accountDao.observeActive().flatMapLatest { account ->
        if (account == null) {
            flowOf(null)
        } else {
            serverDao.observeAll().map { servers ->
                servers.firstOrNull { it.id == account.serverId }?.let { ActiveAccount(it.toModel(), account.toModel()) }
            }
        }
    }

    override suspend fun saveLogin(
        serverUrl: String,
        serverVersion: String?,
        userId: String,
        username: String,
        defaultLibraryId: String?,
    ): ActiveAccount {
        val now = System.currentTimeMillis()
        val server = (serverDao.findByUrl(serverUrl) ?: ServerEntity(UUID.randomUUID().toString(), serverUrl, null, null, now))
            .copy(serverVersion = serverVersion)
        serverDao.upsert(server)
        val account = (accountDao.find(server.id, userId) ?: AccountEntity(UUID.randomUUID().toString(), server.id, userId, username, null, true, now))
            .copy(username = username, defaultLibraryId = defaultLibraryId)
        accountDao.upsertActive(account)
        return ActiveAccount(server.toModel(), account.toModel())
    }

    override suspend fun signOut() = accountDao.deactivateAll()

    private fun ServerEntity.toModel() = Server(id, url, name, serverVersion)
    private fun AccountEntity.toModel() = Account(id, serverId, userId, username, defaultLibraryId)
}
