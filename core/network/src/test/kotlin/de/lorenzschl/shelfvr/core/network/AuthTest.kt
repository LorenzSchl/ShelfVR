package de.lorenzschl.shelfvr.core.network

import de.lorenzschl.shelfvr.core.network.dto.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.Request
import retrofit2.HttpException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AuthTest {

    private val server = MockWebServer()
    private val refreshCalls = AtomicInteger()
    private var refreshSucceeds = true

    @BeforeTest
    fun setUp() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when (request.url.encodedPath) {
                "/abs/login" -> json(loginBody(access = "a1", refresh = "r1"))
                "/abs/auth/refresh" -> {
                    refreshCalls.incrementAndGet()
                    Thread.sleep(100) // parallele Requests sollen sich sicher überschneiden
                    when {
                        !refreshSucceeds -> MockResponse.Builder().code(401).build()
                        request.headers[HEADER_REFRESH_TOKEN] == "r1" -> json(loginBody(access = "a2", refresh = "r2"))
                        else -> MockResponse.Builder().code(401).build()
                    }
                }
                else -> when (request.headers[HEADER_AUTHORIZATION]) {
                    "Bearer a2" -> json("""{"libraries":[]}""")
                    else -> MockResponse.Builder().code(401).body("Unauthorized").build()
                }
            }
        }
        server.start()
    }

    @AfterTest
    fun tearDown() = server.close()

    private val serverUrl get() = server.url("/abs/")
    private val tokenStore = InMemoryTokenStore()
    private val client by lazy { AbsClientFactory.createHttpClient(tokenStore) }
    private val api by lazy { AbsClientFactory.createApi(serverUrl, client) }

    @Test
    fun `Login fordert Tokens an und liest accessToken und refreshToken`() = runBlocking {
        val response = api.login(LoginRequest("user", "pw"))

        val recorded = server.takeRequest()
        assertEquals("true", recorded.headers["x-return-tokens"])
        assertNull(recorded.headers[HEADER_AUTHORIZATION])
        assertEquals("a1", response.user.accessToken)
        assertEquals("r1", response.user.refreshToken)
    }

    @Test
    fun `Bei 401 wird erneuert, rotierte Tokens gespeichert und der Request wiederholt`() = runBlocking {
        tokenStore.update(session(access = "a1", refresh = "r1"))

        api.libraries()

        assertEquals(1, refreshCalls.get())
        assertEquals("a2", tokenStore.session.value?.accessToken)
        assertEquals("r2", tokenStore.session.value?.refreshToken)
    }

    @Test
    fun `Parallele 401er lösen genau einen Refresh aus`() = runBlocking {
        tokenStore.update(session(access = "a1", refresh = "r1"))

        List(8) { async(Dispatchers.IO) { api.libraries() } }.awaitAll()

        assertEquals(1, refreshCalls.get())
    }

    @Test
    fun `Gescheiterter Refresh beendet die Sitzung`() = runBlocking {
        refreshSucceeds = false
        tokenStore.update(session(access = "a1", refresh = "r1"))

        val error = assertFailsWith<HttpException> { api.libraries() }

        assertEquals(401, error.code())
        assertNull(tokenStore.session.value)
    }

    @Test
    fun `Token geht nicht an fremde Hosts`() {
        tokenStore.update(session(access = "a2", refresh = "r2"))
        MockWebServer().use { other ->
            other.enqueue(MockResponse.Builder().code(200).build())
            other.start()

            client.newCall(Request.Builder().url(other.url("/abs/api/libraries")).build()).execute().close()

            assertNull(other.takeRequest().headers[HEADER_AUTHORIZATION])
        }
    }

    private fun session(access: String, refresh: String) = AbsSession(serverUrl, "user", access, refresh)

    private fun json(body: String) =
        MockResponse.Builder().code(200).addHeader("Content-Type", "application/json").body(body).build()

    private fun loginBody(access: String, refresh: String) =
        """{"user":{"id":"u1","username":"user","token":"legacy","accessToken":"$access","refreshToken":"$refresh"},
           "userDefaultLibraryId":"lib1","serverSettings":{"version":"2.37.1"}}"""
}
