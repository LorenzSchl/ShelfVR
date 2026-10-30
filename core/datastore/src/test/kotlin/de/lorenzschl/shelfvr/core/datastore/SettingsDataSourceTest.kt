package de.lorenzschl.shelfvr.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import de.lorenzschl.shelfvr.core.model.AppSettings
import de.lorenzschl.shelfvr.core.model.SortKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsDataSourceTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.source() = SettingsDataSource(
        PreferenceDataStoreFactory.create(scope = backgroundScope) { folder.newFile("settings.preferences_pb").also { it.delete() } },
    )

    @Test
    fun `Ohne gespeicherte Werte gelten die Defaults der Spec`() = runTest {
        assertEquals(AppSettings(), source().settings.first())
    }

    @Test
    fun `Änderungen werden gespeichert`() = runTest {
        val source = source()
        source.update { it.copy(activeLibraryId = "lib1", sortKey = SortKey.ADDED_AT, sortDesc = true, pauseOnHeadsetRemoved = true) }

        val settings = source.settings.first()
        assertEquals("lib1", settings.activeLibraryId)
        assertEquals(SortKey.ADDED_AT, settings.sortKey)
        assertEquals(true, settings.sortDesc)
        assertEquals(true, settings.pauseOnHeadsetRemoved)
    }

    @Test
    fun `HTTP-Zustimmungen lassen sich erteilen und widerrufen`() = runTest {
        val source = source()
        source.setCleartextAllowed("http://192.168.1.20:13378", true)
        source.setCleartextAllowed("http://nas:80", true)
        source.setCleartextAllowed("http://nas:80", false)

        assertEquals(setOf("http://192.168.1.20:13378"), source.cleartextOrigins.first())
    }
}
