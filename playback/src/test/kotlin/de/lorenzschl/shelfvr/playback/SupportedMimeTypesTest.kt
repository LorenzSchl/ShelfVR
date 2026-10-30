package de.lorenzschl.shelfvr.playback

import kotlin.test.Test
import kotlin.test.assertEquals

class SupportedMimeTypesTest {

    @Test
    fun `Typische Decoder ergeben die ABS-Typen`() {
        val decoders = setOf("audio/mpeg", "audio/mp4a-latm", "audio/flac", "audio/opus", "audio/vorbis", "audio/raw")
        assertEquals(
            listOf("audio/mpeg", "audio/mp4", "audio/aac", "audio/x-m4b", "audio/flac", "audio/ogg"),
            SupportedMimeTypes.absMimeTypesFor(decoders),
        )
    }

    @Test
    fun `Ohne Vorbis kein audio ogg`() {
        assertEquals(listOf("audio/mpeg"), SupportedMimeTypes.absMimeTypesFor(setOf("audio/mpeg", "audio/opus")))
    }
}
