package de.lorenzschl.shelfvr.core.network

import kotlin.test.Test
import kotlin.test.assertEquals

class TestEnvTest {

    @Test
    fun `Kommentare, Anführungszeichen und Leerzeilen`() {
        val parsed = TestEnv.parse(
            """
            # Kopfkommentar
            ABS_URL_LOCAL=http://localhost:13378    # für Tests
            ABS_LIBRARY_NAME=Testbücher
            ABS_TEST_PASSWORD="geheim # mit Raute"
            ABS_EMPTY=

            """.trimIndent(),
        )
        assertEquals(
            mapOf(
                "ABS_URL_LOCAL" to "http://localhost:13378",
                "ABS_LIBRARY_NAME" to "Testbücher",
                "ABS_TEST_PASSWORD" to "geheim # mit Raute",
                "ABS_EMPTY" to "",
            ),
            parsed,
        )
    }
}
