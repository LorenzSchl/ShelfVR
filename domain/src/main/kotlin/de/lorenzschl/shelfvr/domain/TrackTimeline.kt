package de.lorenzschl.shelfvr.domain

import de.lorenzschl.shelfvr.core.model.AudioTrack

/** Position innerhalb eines Tracks: Index in der Track-Liste und Millisekunden ab Track-Anfang. */
data class TrackPosition(val trackIndex: Int, val positionMs: Long)

/**
 * Rechnet zwischen Buchzeit (global, wie ABS sie speichert) und Track-Position (wie ExoPlayer sie
 * kennt) um. Die Tracks müssen nach [AudioTrack.startOffsetSeconds] sortiert sein.
 */
class TrackTimeline(private val tracks: List<AudioTrack>) {

    init {
        require(tracks.isNotEmpty()) { "Ein Buch braucht mindestens einen Track." }
        require(tracks.zipWithNext().all { (a, b) -> a.startOffsetSeconds <= b.startOffsetSeconds }) {
            "Tracks müssen nach startOffset sortiert sein."
        }
    }

    val totalDurationSeconds: Double
        get() = tracks.last().let { it.startOffsetSeconds + it.durationSeconds }

    fun toBookTimeSeconds(position: TrackPosition): Double {
        val track = tracks[position.trackIndex.coerceIn(tracks.indices)]
        return track.startOffsetSeconds + position.positionMs / 1000.0
    }

    /** Buchzeiten außerhalb des Buchs werden auf Anfang bzw. Ende begrenzt. */
    fun toTrackPosition(bookTimeSeconds: Double): TrackPosition {
        val clamped = bookTimeSeconds.coerceIn(0.0, totalDurationSeconds)
        val index = tracks.indexOfLast { it.startOffsetSeconds <= clamped }.coerceAtLeast(0)
        val track = tracks[index]
        val offsetMs = ((clamped - track.startOffsetSeconds) * 1000).toLong()
        return TrackPosition(index, offsetMs.coerceAtMost((track.durationSeconds * 1000).toLong()))
    }
}
