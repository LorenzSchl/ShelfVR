package de.lorenzschl.shelfvr.core.network

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * Unverschlüsseltes HTTP nur für Server, denen der Nutzer ausdrücklich zugestimmt hat
 * (PROJECT_SPEC.md §3.2, Rev. 3). Die Zustimmung gilt für genau einen Ursprung
 * (Schema + Host + Port). Die Android-Plattform erlaubt Cleartext, die App setzt die Regel selbst durch.
 */
interface CleartextPolicy {
    fun isAllowed(origin: String): Boolean
    fun allow(origin: String)
    fun revoke(origin: String)
}

/** Zustimmungen im Speicher; die dauerhafte Variante folgt mit der Persistenz (#5). */
class InMemoryCleartextPolicy : CleartextPolicy {
    private val origins = ConcurrentHashMap.newKeySet<String>()
    override fun isAllowed(origin: String) = origin in origins
    override fun allow(origin: String) {
        origins += origin
    }
    override fun revoke(origin: String) {
        origins -= origin
    }
}

/** Ursprung einer URL, z. B. `http://192.168.1.20:13378`. */
val HttpUrl.origin: String get() = "$scheme://$host:$port"

/** HTTP-Request an einen Server ohne Zustimmung; die UI bietet daraufhin das Opt-in an. */
class CleartextNotPermittedException(val origin: String) :
    IOException("Unverschlüsseltes HTTP für $origin nicht freigegeben")

/**
 * Blockiert HTTP-Requests ohne Zustimmung. Wird zweimal eingehängt: als Application-Interceptor
 * (vor dem Verbindungsaufbau) und als Network-Interceptor (greift auch bei Redirects).
 */
class CleartextGuardInterceptor(private val policy: CleartextPolicy) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        if (!url.isHttps && !policy.isAllowed(url.origin)) throw CleartextNotPermittedException(url.origin)
        return chain.proceed(chain.request())
    }
}
