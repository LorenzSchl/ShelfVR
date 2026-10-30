package de.lorenzschl.shelfvr.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LibrariesResponse(val libraries: List<LibraryDto>)

@Serializable
data class LibraryDto(val id: String, val name: String, val mediaType: String)

@Serializable
data class LibraryItemsResponse(
    val results: List<LibraryItemDto>,
    val total: Int,
    val limit: Int = 0,
    val page: Int = 0,
)

@Serializable
data class LibraryItemDto(val id: String, val media: BookMediaDto)

@Serializable
data class BookMediaDto(
    val metadata: BookMetadataDto,
    val duration: Double? = null,
    val numTracks: Int? = null,
    val numChapters: Int? = null,
)

@Serializable
data class BookMetadataDto(val title: String? = null, val authorName: String? = null)
