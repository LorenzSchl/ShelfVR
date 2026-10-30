package de.lorenzschl.shelfvr.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.lorenzschl.shelfvr.core.database.ShelfVrDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class RoomAccountRepositoryTest {

    private val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), ShelfVrDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val repository = RoomAccountRepository(db.serverDao(), db.accountDao())

    @AfterTest
    fun tearDown() = db.close()

    @Test
    fun `Erneuter Login aktualisiert Server und Account statt sie zu verdoppeln`() = runTest {
        val first = repository.saveLogin("https://abs.example.org/", "2.36.0", "u1", "lorenz", "lib1")
        val second = repository.saveLogin("https://abs.example.org/", "2.37.1", "u1", "lorenz", "lib2")

        assertEquals(first.server.id, second.server.id)
        assertEquals(first.account.id, second.account.id)
        assertEquals("2.37.1", repository.activeAccount.first()?.server?.serverVersion)
        assertEquals("lib2", repository.activeAccount.first()?.account?.defaultLibraryId)
    }

    @Test
    fun `Zweiter Account wird aktiv, Abmelden beendet die Auswahl`() = runTest {
        repository.saveLogin("https://abs.example.org/", null, "u1", "lorenz", null)
        repository.saveLogin("http://192.168.1.20:13378/", null, "u9", "gast", null)

        assertEquals("gast", repository.activeAccount.first()?.account?.username)

        repository.signOut()
        assertNull(repository.activeAccount.first())
    }
}
