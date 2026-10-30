package de.lorenzschl.shelfvr.core.model

/** Wiedergabezustand, wie ihn jedes Fenster über seinen MediaController sieht. */
data class PlaybackSnapshot(
    val title: String?,
    val subtitle: String?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
) {
    val hasMedia: Boolean get() = title != null

    companion object {
        val Empty = PlaybackSnapshot(
            title = null,
            subtitle = null,
            isPlaying = false,
            positionMs = 0,
            durationMs = 0,
        )
    }
}
