package de.lorenzschl.shelfvr.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Upsert
    suspend fun upsert(server: ServerEntity)

    @Query("SELECT * FROM server WHERE url = :url")
    suspend fun findByUrl(url: String): ServerEntity?

    @Query("SELECT * FROM server WHERE id = :id")
    suspend fun findById(id: String): ServerEntity?

    @Query("SELECT * FROM server ORDER BY createdAt")
    fun observeAll(): Flow<List<ServerEntity>>
}

@Dao
interface AccountDao {
    @Upsert
    suspend fun upsert(account: AccountEntity)

    @Query("SELECT * FROM account WHERE serverId = :serverId AND userId = :userId")
    suspend fun find(serverId: String, userId: String): AccountEntity?

    @Query("SELECT * FROM account WHERE active = 1 LIMIT 1")
    fun observeActive(): Flow<AccountEntity?>

    @Query("SELECT * FROM account WHERE active = 1 LIMIT 1")
    suspend fun active(): AccountEntity?

    @Query("UPDATE account SET active = (id = :accountId)")
    suspend fun activateOnly(accountId: String)

    @Query("UPDATE account SET active = 0")
    suspend fun deactivateAll()

    /** Speichert den Account und macht ihn als einzigen aktiv. */
    @Transaction
    suspend fun upsertActive(account: AccountEntity) {
        upsert(account.copy(active = true))
        activateOnly(account.id)
    }
}

@Dao
interface ShelfConfigDao {
    @Upsert
    suspend fun upsert(config: ShelfConfigEntity)

    @Query("SELECT * FROM shelf_config WHERE id = :id")
    suspend fun findById(id: String): ShelfConfigEntity?

    @Query("SELECT * FROM shelf_config ORDER BY createdAt")
    fun observeAll(): Flow<List<ShelfConfigEntity>>

    @Query("DELETE FROM shelf_config WHERE id = :id")
    suspend fun delete(id: String)
}
