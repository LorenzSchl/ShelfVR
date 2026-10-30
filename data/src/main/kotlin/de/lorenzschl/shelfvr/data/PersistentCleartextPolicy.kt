package de.lorenzschl.shelfvr.data

import android.os.Looper
import de.lorenzschl.shelfvr.core.datastore.SettingsDataSource
import de.lorenzschl.shelfvr.core.network.CleartextPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** HTTP-Zustimmungen (§3.2) dauerhaft in den Einstellungen; Abfragen aus einem Speicher-Cache. */
class PersistentCleartextPolicy(
    private val settings: SettingsDataSource,
    private val scope: CoroutineScope,
    private val isMainThread: () -> Boolean = { Looper.myLooper() == Looper.getMainLooper() },
) : CleartextPolicy {

    private val cache = MutableStateFlow<Set<String>?>(null)

    init {
        scope.launch { settings.cleartextOrigins.collect { stored -> cache.value = stored } }
    }

    override fun isAllowed(origin: String): Boolean = origin in loaded()

    override fun allow(origin: String) = change(origin, allowed = true)

    override fun revoke(origin: String) = change(origin, allowed = false)

    /** Erst den gespeicherten Stand abwarten, sonst überschreibt das erste Laden die Änderung. */
    private fun change(origin: String, allowed: Boolean) {
        loaded()
        cache.update { current -> current.orEmpty().let { if (allowed) it + origin else it - origin } }
        if (isMainThread()) {
            scope.launch { settings.setCleartextAllowed(origin, allowed) }
        } else {
            runBlocking { settings.setCleartextAllowed(origin, allowed) }
        }
    }

    private fun loaded(): Set<String> = cache.value ?: runBlocking { cache.filterNotNull().first() }
}
