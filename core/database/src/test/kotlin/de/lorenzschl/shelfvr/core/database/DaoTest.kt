package de.lorenzschl.shelfvr.core.database

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class DaoTest {

    private lateinit var db: ShelfVrDatabase

    @BeforeTest
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), ShelfVrDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @AfterTest
    fun tearDown() = db.close()

    private val server = ServerEntity("s1", "https://abs.example.org/", null, "2.37.1", 1)

    @Test
    fun `Nur ein Account ist aktiv`() = runTest {
        db.serverDao().upsert(server)
        db.accountDao().upsertActive(account("a1", "u1"))
        db.accountDao().upsertActive(account("a2", "u2"))

        assertEquals("a2", db.accountDao().observeActive().first()?.id)
        assertEquals(false, db.accountDao().find("s1", "u1")?.active)
    }

    @Test
    fun `Server-URL ist eindeutig`() = runTest {
        // @Upsert fängt die Verletzung ab und aktualisiert ins Leere; das Repository prüft deshalb
        // vorher per findByUrl. Hier wird nur der Unique-Index selbst geprüft.
        db.serverDao().upsert(server)
        assertFailsWith<SQLiteConstraintException> {
            db.openHelper.writableDatabase.execSQL(
                "INSERT INTO server (id, url, name, serverVersion, createdAt) VALUES ('s2', '${server.url}', NULL, NULL, 2)",
            )
        }
    }

    @Test
    fun `Löschen eines Servers entfernt Accounts und Regale`() = runTest {
        db.serverDao().upsert(server)
        db.accountDao().upsertActive(account("a1", "u1"))
        db.shelfConfigDao().upsert(ShelfConfigEntity("shelf1", "s1", "lib1", null, "TITLE", false, null, 0, null, 1))

        db.openHelper.writableDatabase.execSQL("DELETE FROM server WHERE id = 's1'")

        assertNull(db.accountDao().active())
        assertNull(db.shelfConfigDao().findById("shelf1"))
    }

    private fun account(id: String, userId: String) = AccountEntity(id, "s1", userId, "name-$userId", null, false, 1)
}
