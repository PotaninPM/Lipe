package com.example.lipe

import java.security.Key
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import android.util.Base64
object DeCryptMessages {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES"

    fun generateKey(): Key {
        val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(256)
        return keyGen.generateKey()
    }

    fun encrypt(data: String, secretKey: Key): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedValue = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedValue, Base64.DEFAULT)
    }

    fun decrypt(data: String, secretKey: Key): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedValue = cipher.doFinal(Base64.decode(data, Base64.DEFAULT))
        return String(decryptedValue, Charsets.UTF_8)
    }
}