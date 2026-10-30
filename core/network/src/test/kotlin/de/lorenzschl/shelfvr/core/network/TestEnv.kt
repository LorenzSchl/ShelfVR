package de.lorenzschl.shelfvr.core.network

import java.io.File

/**
 * Liest `.env.test` (Pfad kommt von Gradle). Format wie in der Shell: `KEY=wert  # Kommentar`,
 * Werte optional in Anführungszeichen. Fehlt die Datei, ist die Map leer.
 */
object TestEnv {

    private val values: Map<String, String> by lazy {
        val file = System.getProperty("shelfvr.envTest")?.let(::File)?.takeIf { it.isFile }
        file?.readText()?.let(::parse).orEmpty()
    }

    operator fun get(key: String): String? = values[key]?.takeIf { it.isNotBlank() }

    internal fun parse(text: String): Map<String, String> = text.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && '=' in it }
        .associate { line ->
            val key = line.substringBefore('=').trim()
            val raw = line.substringAfter('=').trim()
            val value = when {
                raw.startsWith('"') -> raw.drop(1).substringBefore('"')
                raw.startsWith('\'') -> raw.drop(1).substringBefore('\'')
                else -> raw.split(Regex("\\s+#"), limit = 2)[0].trim()
            }
            key to value
        }
}
