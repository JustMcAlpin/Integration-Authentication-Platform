// Crypto.kt
package com.example.integrationauthenticationplatform.crypto

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object KeyProvider {
    fun keyBytes(): ByteArray {
        val b64 = com.example.integrationauthenticationplatform.BuildConfig.ENCRYPTION_KEY_B64
        require(b64.isNotEmpty()) { "ENCRYPTION_KEY missing" }
        return Base64.decode(b64, Base64.DEFAULT)
    }
}

object Crypto {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_BYTES = 12
    private val rng = SecureRandom()

    data class Box(val cipherB64: String, val ivB64: String)

    fun encryptJson(json: String): Box {
        val keyB64 = com.example.integrationauthenticationplatform.BuildConfig.ENCRYPTION_KEY_B64
        require(keyB64.isNotEmpty()) { "ENCRYPTION_KEY missing" }
        val key = SecretKeySpec(Base64.decode(keyB64, Base64.DEFAULT), "AES")
        val iv = ByteArray(IV_BYTES).also { rng.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val out = cipher.doFinal(json.toByteArray(Charsets.UTF_8))
        return Box(
            Base64.encodeToString(out, Base64.NO_WRAP),
            Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    fun decryptToJson(cipherB64: String, ivB64: String): String {
        val keyB64 = com.example.integrationauthenticationplatform.BuildConfig.ENCRYPTION_KEY_B64
        require(keyB64.isNotEmpty()) { "ENCRYPTION_KEY missing" }
        val key = SecretKeySpec(Base64.decode(keyB64, Base64.DEFAULT), "AES")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = Base64.decode(ivB64, Base64.DEFAULT)
        val inBytes = Base64.decode(cipherB64, Base64.DEFAULT)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(inBytes), Charsets.UTF_8)
    }
}
