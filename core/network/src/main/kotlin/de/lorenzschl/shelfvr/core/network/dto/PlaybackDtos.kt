package de.lorenzschl.shelfvr.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlayRequest(
    val deviceInfo: DeviceInfoDto,
    val mediaPlayer: String,
    val supportedMimeTypes: List<String>,
    val forceDirectPlay: Boolean = false,
    val forceTranscode: Boolean = false,
)

@Serializable
data class DeviceInfoDto(
    val clientName: String,
    val clientVersion: String,
    val deviceId: String,
    val manufacturer: String,
    val model: String,
    val sdkVersion: Int,
)

@Serializable
data class PlaybackSessionDto(
    val id: String,
    val libraryItemId: String,
    val displayTitle: String? = null,
    val displayAuthor: String? = null,
    val duration: Double,
    val currentTime: Double,
    /** 0 = Direct Play, 1 = Direct Stream, 2 = Transcode (HLS), 3 = Local. */
    val playMethod: Int,
    val audioTracks: List<AudioTrackDto>,
    val chapters: List<ChapterDto> = emptyList(),
)

@Serializable
data class AudioTrackDto(
    val index: Int,
    val startOffset: Double,
    val duration: Double,
    val title: String? = null,
    /** Relativ zur Server-URL, z. B. `/api/items/{id}/file/{ino}` oder `/hls/{session}/output.m3u8`. */
    val contentUrl: String,
    val mimeType: String? = null,
)

@Serializable
data class ChapterDto(val id: Int, val start: Double, val end: Double, val title: String)

@Serializable
data class SyncRequest(val currentTime: Double, val timeListened: Double, val duration: Double)
