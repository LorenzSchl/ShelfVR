package de.lorenzschl.shelfvr.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServerUrlTest {

    @Test
    fun `Schema https wird ergänzt und Slash angehängt`() {
        assertEquals("https://abs.example.org/", ServerUrl.normalize(" abs.example.org ").toString())
    }

    @Test
    fun `http mit Port bleibt erhalten`() {
        assertEquals("http://192.168.1.20:13378/", ServerUrl.normalize("http://192.168.1.20:13378").toString())
    }

    @Test
    fun `Subpfad bekommt Trailing Slash, Query fällt weg`() {
        assertEquals(
            "https://host.example/audiobookshelf/",
            ServerUrl.normalize("https://host.example/audiobookshelf?x=1#y").toString(),
        )
    }

    @Test
    fun `Leere oder kaputte Eingabe ergibt null`() {
        assertNull(ServerUrl.normalize("   "))
        assertNull(ServerUrl.normalize("ftp://host"))
    }

    @Test
    fun `contentUrl wird relativ zum Subpfad aufgelöst`() {
        val server = ServerUrl.normalize("https://host.example/audiobookshelf")!!
        assertEquals(
            "https://host.example/audiobookshelf/api/items/1/file/2",
            server.resolveServerPath("/api/items/1/file/2").toString(),
        )
    }
}
