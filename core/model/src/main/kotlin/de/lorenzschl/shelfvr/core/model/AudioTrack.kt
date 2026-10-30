package de.lorenzschl.shelfvr.core.model

/**
 * Eine Audiodatei eines Buchs. [startOffsetSeconds] ist die Buchzeit, an der die Datei beginnt
 * (PROJECT_SPEC.md §6.3).
 */
data class AudioTrack(
    val index: Int,
    val startOffsetSeconds: Double,
    val durationSeconds: Double,
    val contentUrl: String,
    val mimeType: String?,
)
