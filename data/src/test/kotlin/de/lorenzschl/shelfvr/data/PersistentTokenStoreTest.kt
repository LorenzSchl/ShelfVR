package de.lorenzschl.shelfvr.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import de.lorenzschl.shelfvr.core.network.AbsSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.Base64
import kotlin.concurrent.thread
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class PersistentTokenStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val file by lazy { File(folder.root, "tokens.preferences_pb") }
    private val dataStore: DataStore<Preferences> by lazy { PreferenceDataStoreFactory.create(scope = io) { file } }
    private val session = AbsSession("https://abs.example.org/".toHttpUrl(), "lorenz", "secret-access", "secret-refresh")

    @AfterTest
    fun tearDown() = io.cancel()

    private fun cipher(): TokenCipher {
        AeadConfig.register()
        val aead = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        return AeadTokenCipher { aead }
    }

    private val cipher = cipher()

    /** Schreibt von einem Hintergrund-Thread, also synchron – wie beim Token-Refresh in OkHttp. */
    private fun PersistentTokenStore.updateFromBackground(value: AbsSession?) = thread { update(value) }.join()

    @Test
    fun `Sitzung übersteht einen Neustart`() = runBlocking {
        val first = PersistentTokenStore(dataStore, cipher, io).apply { awaitRestored() }
        first.updateFromBackground(session)

        val second = PersistentTokenStore(dataStore, cipher, io).apply { awaitRestored() }

        assertEquals(session, second.session.value)
    }

    @Test
    fun `Tokens liegen nicht im Klartext vor`() = runBlocking {
        PersistentTokenStore(dataStore, cipher, io).apply { awaitRestored() }.updateFromBackground(session)

        val stored = dataStore.data.first()[stringPreferencesKey("session")]!!
        val bytes = String(Base64.getDecoder().decode(stored), Charsets.ISO_8859_1)
        assertFalse("secret-access" in bytes || "secret-refresh" in bytes)
        assertFalse("secret" in file.readText(Charsets.ISO_8859_1))
    }

    @Test
    fun `Nicht entschlüsselbare Daten führen zu neuer Anmeldung`() = runBlocking {
        PersistentTokenStore(dataStore, cipher, io).apply { awaitRestored() }.updateFromBackground(session)

        val withOtherKey = PersistentTokenStore(dataStore, cipher(), io).apply { awaitRestored() }

        assertNull(withOtherKey.session.value)
        assertNull(dataStore.data.first()[stringPreferencesKey("session")])
    }

    @Test
    fun `Abmelden löscht die gespeicherte Sitzung`() = runBlocking {
        val store = PersistentTokenStore(dataStore, cipher, io).apply { awaitRestored() }
        store.updateFromBackground(session)
        store.updateFromBackground(null)

        assertNull(PersistentTokenStore(dataStore, cipher, io).apply { awaitRestored() }.session.value)
    }

    @Test
    fun `Neuere Sitzung gewinnt gegen späte Wiederherstellung`() = runBlocking {
        PersistentTokenStore(dataStore, cipher, io).apply { awaitRestored() }.updateFromBackground(session)
        val newer = session.copy(accessToken = "newer-access", refreshToken = "newer-refresh")

        // Wiederherstellung läuft erst, wenn der Test-Scheduler weiterläuft.
        val restoreScope = TestScope(StandardTestDispatcher())
        val store = PersistentTokenStore(dataStore, cipher, restoreScope)
        store.updateFromBackground(newer)
        restoreScope.testScheduler.advanceUntilIdle()

        assertEquals(newer, store.session.value)
    }
}
