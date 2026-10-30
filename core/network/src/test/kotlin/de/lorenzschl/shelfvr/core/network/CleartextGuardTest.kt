package de.lorenzschl.shelfvr.core.network

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CleartextGuardTest {

    private val server = MockWebServer().apply { start() }
    private val policy = InMemoryCleartextPolicy()
    private val client: OkHttpClient = AbsClientFactory.createHttpClient(InMemoryTokenStore(), policy)

    @AfterTest
    fun tearDown() = server.close()

    private fun get(url: okhttp3.HttpUrl) = client.newCall(Request.Builder().url(url).build()).execute()

    @Test
    fun `HTTP ohne Zustimmung wird vor dem Verbindungsaufbau abgelehnt`() {
        val error = assertFailsWith<CleartextNotPermittedException> { get(server.url("/ping")) }

        assertEquals(server.url("/").origin, error.origin)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `HTTP mit Zustimmung für genau diesen Server funktioniert`() {
        server.enqueue(MockResponse.Builder().body("ok").build())
        policy.allow(server.url("/").origin)

        get(server.url("/ping")).use { assertEquals(200, it.code) }
    }

    @Test
    fun `Redirect auf einen nicht freigegebenen HTTP-Server wird blockiert`() {
        MockWebServer().use { other ->
            other.start()
            policy.allow(server.url("/").origin)
            server.enqueue(MockResponse.Builder().code(302).addHeader("Location", other.url("/x").toString()).build())

            assertFailsWith<CleartextNotPermittedException> { get(server.url("/start")) }
            assertEquals(0, other.requestCount)
        }
    }

    @Test
    fun `Zustimmung gilt pro Ursprung inklusive Port und lässt sich widerrufen`() {
        policy.allow("http://192.168.1.20:13378")

        assertTrue(policy.isAllowed("http://192.168.1.20:13378/".toHttpUrl().origin))
        assertFalse(policy.isAllowed("http://192.168.1.20:8080/".toHttpUrl().origin))
        assertFalse(policy.isAllowed("https://192.168.1.20:13378/".toHttpUrl().origin))

        policy.revoke("http://192.168.1.20:13378")
        assertFalse(policy.isAllowed("http://192.168.1.20:13378"))
    }

    @Test
    fun `Redirects zwischen HTTPS und HTTP sind abgeschaltet`() {
        assertFalse(client.followSslRedirects)
    }
}
