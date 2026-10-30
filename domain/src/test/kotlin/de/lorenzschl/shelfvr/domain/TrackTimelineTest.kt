package de.lorenzschl.shelfvr.domain

import de.lorenzschl.shelfvr.core.model.AudioTrack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TrackTimelineTest {

    // Entspricht dem Testbuch „Multi-MP3 Buch“: 3 Tracks à 300 s.
    private val timeline = TrackTimeline(
        listOf(
            track(index = 1, start = 0.0, duration = 300.0),
            track(index = 2, start = 300.0, duration = 300.0),
            track(index = 3, start = 600.0, duration = 300.0),
        ),
    )

    @Test
    fun `Buchzeit im zweiten Track wird auf Track-Position umgerechnet`() {
        assertEquals(TrackPosition(trackIndex = 1, positionMs = 42_500), timeline.toTrackPosition(342.5))
    }

    @Test
    fun `Track-Grenze gehört zum folgenden Track`() {
        assertEquals(TrackPosition(trackIndex = 1, positionMs = 0), timeline.toTrackPosition(300.0))
    }

    @Test
    fun `Track-Position wird in Buchzeit umgerechnet`() {
        assertEquals(642.5, timeline.toBookTimeSeconds(TrackPosition(trackIndex = 2, positionMs = 42_500)))
    }

    @Test
    fun `Buchzeit außerhalb des Buchs wird begrenzt`() {
        assertEquals(TrackPosition(0, 0), timeline.toTrackPosition(-5.0))
        assertEquals(TrackPosition(2, 300_000), timeline.toTrackPosition(10_000.0))
    }

    @Test
    fun `Hin- und Rückrechnung ergibt dieselbe Buchzeit`() {
        listOf(0.0, 12.25, 299.999, 300.0, 599.5, 899.0).forEach { bookTime ->
            assertEquals(bookTime, timeline.toBookTimeSeconds(timeline.toTrackPosition(bookTime)), 0.001)
        }
    }

    @Test
    fun `Leere Track-Liste wird abgelehnt`() {
        assertFailsWith<IllegalArgumentException> { TrackTimeline(emptyList()) }
    }

    private fun track(index: Int, start: Double, duration: Double) =
        AudioTrack(index, start, duration, contentUrl = "/api/items/x/file/$index", mimeType = "audio/mpeg")
}
