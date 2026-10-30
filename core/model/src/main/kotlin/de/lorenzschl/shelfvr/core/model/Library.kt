package de.lorenzschl.shelfvr.core.model

/** Sortierung für Item-Grid und Regalfenster (FR-12, R-02). */
enum class SortKey { TITLE, AUTHOR, ADDED_AT, PUBLISHED_YEAR, DURATION }

/** Konfiguration eines Regalfensters (PROJECT_SPEC.md §9.4); schon im MVP persistiert. */
data class ShelfConfig(
    val id: String,
    val serverId: String,
    val libraryId: String,
    val title: String?,
    val sortKey: SortKey,
    val sortDesc: Boolean,
    val filter: String?,
    val firstVisibleIndex: Int,
    val chainedToShelfId: String?,
    val createdAt: Long,
)
