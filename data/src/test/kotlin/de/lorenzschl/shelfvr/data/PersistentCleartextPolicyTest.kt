package de.lorenzschl.shelfvr.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import de.lorenzschl.shelfvr.core.datastore.SettingsDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersistentCleartextPolicyTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val settings by lazy {
        SettingsDataSource(PreferenceDataStoreFactory.create(scope = io) { File(folder.root, "settings.preferences_pb") })
    }

    @AfterTest
    fun tearDown() = io.cancel()

    @Test
    fun `Zustimmung wird gespeichert und nach Neustart wieder geladen`() = runBlocking {
        val origin = "http://192.168.1.20:13378"
        val policy = PersistentCleartextPolicy(settings, io, isMainThread = { false })
        assertFalse(policy.isAllowed(origin))

        policy.allow(origin)
        assertTrue(policy.isAllowed(origin))
        // Abseits des Main-Threads wird synchron gespeichert.
        assertTrue(origin in settings.cleartextOrigins.first())

        assertTrue(PersistentCleartextPolicy(settings, io, isMainThread = { false }).isAllowed(origin))
    }

    @Test
    fun `Widerruf wirkt sofort`() {
        val policy = PersistentCleartextPolicy(settings, io, isMainThread = { false })
        policy.allow("http://nas:80")
        policy.revoke("http://nas:80")

        assertFalse(policy.isAllowed("http://nas:80"))
    }
}
