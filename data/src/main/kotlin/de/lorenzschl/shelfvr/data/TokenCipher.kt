package de.lorenzschl.shelfvr.data

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

/** Verschlüsselt die gespeicherte Sitzung (FR-02, NFR-05). */
interface TokenCipher {
    fun encrypt(plain: ByteArray): ByteArray
    fun decrypt(encrypted: ByteArray): ByteArray
}

class AeadTokenCipher(private val aead: () -> Aead) : TokenCipher {
    override fun encrypt(plain: ByteArray): ByteArray = aead().encrypt(plain, ASSOCIATED_DATA)
    override fun decrypt(encrypted: ByteArray): ByteArray = aead().decrypt(encrypted, ASSOCIATED_DATA)

    private companion object {
        val ASSOCIATED_DATA = "shelfvr-session-v1".toByteArray()
    }
}

/**
 * AES-256-GCM-Keyset, selbst verschlüsselt mit einem Schlüssel im Android Keystore.
 * Wird erst beim ersten Zugriff erzeugt, weil der Keystore spürbar Zeit braucht.
 */
fun keystoreTokenCipher(context: Context): TokenCipher {
    val appContext = context.applicationContext
    val aead by lazy {
        AeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(appContext, "shelfvr_keyset", "shelfvr_keys")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://shelfvr_master_key")
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }
    return AeadTokenCipher { aead }
}
