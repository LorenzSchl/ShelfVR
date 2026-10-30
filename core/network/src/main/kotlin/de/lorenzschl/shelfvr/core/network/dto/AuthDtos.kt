package de.lorenzschl.shelfvr.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)

/** Antwort von `POST /login`, `POST /auth/refresh` und `POST /api/authorize`. */
@Serializable
data class LoginResponse(
    val user: UserDto,
    val userDefaultLibraryId: String? = null,
    val serverSettings: ServerSettingsDto? = null,
)

/** Das veraltete Feld `token` wird bewusst nicht gelesen (PROJECT_SPEC.md §6.1). */
@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
)

@Serializable
data class ServerSettingsDto(val version: String? = null)

@Serializable
data class ServerStatusDto(
    val app: String? = null,
    val serverVersion: String? = null,
    val isInit: Boolean = false,
)
