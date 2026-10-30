package de.lorenzschl.shelfvr.core.model

/** Ein ABS-Server. Das Datenmodell erlaubt mehrere (FR-05), die UI zeigt im MVP einen. */
data class Server(
    val id: String,
    /** Normalisierte URL mit Trailing Slash, z. B. `https://abs.example.org/`. */
    val url: String,
    val name: String?,
    val serverVersion: String?,
)

/** Ein Benutzerkonto auf einem [Server]. Tokens liegen getrennt und verschlüsselt (FR-02). */
data class Account(
    val id: String,
    val serverId: String,
    val userId: String,
    val username: String,
    val defaultLibraryId: String?,
)
