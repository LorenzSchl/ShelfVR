package de.lorenzschl.shelfvr.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "server", indices = [Index(value = ["url"], unique = true)])
data class ServerEntity(
    @PrimaryKey val id: String,
    val url: String,
    val name: String?,
    val serverVersion: String?,
    val createdAt: Long,
)

@Entity(
    tableName = "account",
    foreignKeys = [
        ForeignKey(entity = ServerEntity::class, parentColumns = ["id"], childColumns = ["serverId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("serverId"), Index(value = ["serverId", "userId"], unique = true)],
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val serverId: String,
    val userId: String,
    val username: String,
    val defaultLibraryId: String?,
    /** Genau ein Account ist aktiv; die UI zeigt im MVP nur diesen (FR-05). */
    val active: Boolean,
    val createdAt: Long,
)

/** PROJECT_SPEC.md §9.4. `sortKey` als Name von [de.lorenzschl.shelfvr.core.model.SortKey]. */
@Entity(
    tableName = "shelf_config",
    foreignKeys = [
        ForeignKey(entity = ServerEntity::class, parentColumns = ["id"], childColumns = ["serverId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("serverId")],
)
data class ShelfConfigEntity(
    @PrimaryKey val id: String,
    val serverId: String,
    val libraryId: String,
    val title: String?,
    val sortKey: String,
    val sortDesc: Boolean,
    val filter: String?,
    val firstVisibleIndex: Int,
    val chainedToShelfId: String?,
    val createdAt: Long,
)
