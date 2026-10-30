package de.lorenzschl.shelfvr.core.network

import de.lorenzschl.shelfvr.core.network.dto.DeviceInfoDto
import de.lorenzschl.shelfvr.core.network.dto.LoginRequest
import de.lorenzschl.shelfvr.core.network.dto.PlayRequest
import de.lorenzschl.shelfvr.core.network.dto.SyncRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.junit.Assume.assumeTrue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Läuft gegen den lokalen ABS-Testserver (CLAUDE.md). Ohne `.env.test` wird er übersprungen.
 * Erwartet das Testbuch „Multi-MP3 Buch“ (3 MP3s à 300 s) aus `~/abs-test/make-testdata.sh`.
 */
class AbsServerIntegrationTest {

    private fun env(key: String): String? = TestEnv[key]

    private lateinit var tokenStore: InMemoryTokenStore
    private lateinit var api: AbsApi
    private lateinit var client: okhttp3.OkHttpClient
    private val serverUrl by lazy { ServerUrl.normalize(env("ABS_URL_LOCAL")!!)!! }

    @BeforeTest
    fun setUp() {
        assumeTrue("ABS-Testserver nicht konfiguriert (.env.test fehlt)", env("ABS_URL_LOCAL") != null)
        tokenStore = InMemoryTokenStore()
        client = AbsClientFactory.createHttpClient(tokenStore)
        api = AbsClientFactory.createApi(serverUrl, client)
    }

    private suspend fun login() {
        val response = api.login(LoginRequest(env("ABS_TEST_USER")!!, env("ABS_TEST_PASSWORD")!!))
        tokenStore.update(
            AbsSession(serverUrl, response.user.username, response.user.accessToken!!, response.user.refreshToken!!),
        )
    }

    @Test
    fun `Login, Autorisierung und Bibliotheken`() = runBlocking {
        login()

        assertEquals(env("ABS_VERSION"), api.authorize().serverSettings?.version)
        val libraries = api.libraries().libraries
        assertTrue(libraries.any { it.id == env("ABS_LIBRARY_ID") && it.mediaType == "book" })
    }

    @Test
    fun `Ungültiger Access-Token wird transparent erneuert`() = runBlocking {
        login()
        val before = tokenStore.session.value!!
        tokenStore.update(before.copy(accessToken = "ungueltig"))

        api.libraries()

        val after = tokenStore.session.value!!
        assertNotEquals("ungueltig", after.accessToken)
        assertNotEquals(before.refreshToken, after.refreshToken, "Refresh-Token muss rotiert sein")
    }

    @Test
    fun `Multi-Track-Buch abspielen, streamen, syncen, schließen`() = runBlocking {
        login()
        val item = api.libraryItems(env("ABS_LIBRARY_ID")!!, limit = 100).results
            .first { it.media.metadata.title == "Multi-MP3 Buch" }

        val session = api.play(
            item.id,
            PlayRequest(
                deviceInfo = DeviceInfoDto("ShelfVR-Test", "0.0.0", "integration-test", "test", "jvm", 34),
                mediaPlayer = "ExoPlayer",
                supportedMimeTypes = listOf("audio/mpeg", "audio/mp4"),
            ),
        )
        assertEquals(0, session.playMethod, "MP3 muss direkt abgespielt werden")
        assertEquals(listOf(0.0, 300.0, 600.0), session.audioTracks.map { it.startOffset })

        // Stream mit Auth-Header über dieselbe OkHttp-Instanz
        val streamUrl = serverUrl.resolveServerPath(session.audioTracks[1].contentUrl)
        client.newCall(Request.Builder().url(streamUrl).header("Range", "bytes=0-999").build()).execute().use {
            assertEquals(206, it.code)
            assertEquals("audio/mpeg", it.header("Content-Type"))
        }

        api.syncSession(session.id, SyncRequest(currentTime = 12.0, timeListened = 12.0, duration = session.duration))
        api.closeSession(session.id, SyncRequest(currentTime = 13.0, timeListened = 1.0, duration = session.duration))
    }
}
