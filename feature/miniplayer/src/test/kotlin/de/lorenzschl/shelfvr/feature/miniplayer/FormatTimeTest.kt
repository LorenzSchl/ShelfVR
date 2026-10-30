package de.lorenzschl.shelfvr.feature.miniplayer

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTimeTest {

    @Test
    fun `Minuten unter einer Stunde, Stunden darüber`() {
        assertEquals("0:00", formatTime(0))
        assertEquals("4:05", formatTime(245_999))
        assertEquals("12:15:00", formatTime(44_100_000))
    }
}
