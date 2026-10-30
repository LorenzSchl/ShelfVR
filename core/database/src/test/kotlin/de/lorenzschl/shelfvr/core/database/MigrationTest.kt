package de.lorenzschl.shelfvr.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Gerüst für Migrationstests. Für jede neue Schema-Version hier einen Test ergänzen:
 * `helper.createDatabase(NAME, alt)` → Daten einfügen → `helper.runMigrationsAndValidate(NAME, neu, true, MIGRATION_x_y)`.
 */
@RunWith(RobolectricTestRunner::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), ShelfVrDatabase::class.java)

    @Test
    fun `Exportiertes Schema 1 lässt sich anlegen`() {
        helper.createDatabase(NAME, 1).close()
    }

    private companion object {
        const val NAME = "migration-test.db"
    }
}
