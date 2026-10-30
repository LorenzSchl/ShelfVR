package de.lorenzschl.shelfvr.core.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Minimale Normalisierung der Server-URL: Schema ergänzen (Default https), Trailing Slash
 * erzwingen, Query und Fragment verwerfen. Die vollständige Prüfung folgt mit FR-01 (M1).
 */
object ServerUrl {

    fun normalize(input: String): HttpUrl? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val withScheme = if ("://" in trimmed) trimmed else "https://$trimmed"
        val parsed = withScheme.toHttpUrlOrNull() ?: return null
        val path = parsed.encodedPath.let { if (it.endsWith("/")) it else "$it/" }
        return parsed.newBuilder().encodedPath(path).query(null).fragment(null).build()
    }
}

/** Löst einen vom Server gelieferten Pfad (z. B. `contentUrl`) relativ zur Server-URL auf. */
fun HttpUrl.resolveServerPath(path: String): HttpUrl =
    requireNotNull(resolve(path.removePrefix("/"))) { "Ungültiger Server-Pfad" }
