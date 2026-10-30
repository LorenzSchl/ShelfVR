package de.lorenzschl.shelfvr.core.network

import de.lorenzschl.shelfvr.core.network.dto.LibrariesResponse
import de.lorenzschl.shelfvr.core.network.dto.LibraryItemsResponse
import de.lorenzschl.shelfvr.core.network.dto.LoginRequest
import de.lorenzschl.shelfvr.core.network.dto.LoginResponse
import de.lorenzschl.shelfvr.core.network.dto.PlayRequest
import de.lorenzschl.shelfvr.core.network.dto.PlaybackSessionDto
import de.lorenzschl.shelfvr.core.network.dto.ServerStatusDto
import de.lorenzschl.shelfvr.core.network.dto.SyncRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ABS-Endpunkte aus PROJECT_SPEC.md §6.2, geprüft gegen ABS 2.37.1.
 * Pfade sind relativ (ohne führenden Slash), damit Subpfad-Deployments funktionieren.
 */
interface AbsApi {

    @GET("status")
    suspend fun status(): ServerStatusDto

    @Headers("x-return-tokens: true")
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/authorize")
    suspend fun authorize(): LoginResponse

    @GET("api/libraries")
    suspend fun libraries(): LibrariesResponse

    @GET("api/libraries/{id}/items")
    suspend fun libraryItems(
        @Path("id") libraryId: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int = 0,
        @Query("sort") sort: String? = null,
        @Query("desc") desc: Int? = null,
        @Query("minified") minified: Int = 1,
    ): LibraryItemsResponse

    @POST("api/items/{id}/play")
    suspend fun play(@Path("id") libraryItemId: String, @Body request: PlayRequest): PlaybackSessionDto

    @POST("api/session/{id}/sync")
    suspend fun syncSession(@Path("id") sessionId: String, @Body request: SyncRequest)

    @POST("api/session/{id}/close")
    suspend fun closeSession(@Path("id") sessionId: String, @Body request: SyncRequest)
}
