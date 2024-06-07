package com.example.lipe

import java.security.Key
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import android.util.Base64
import java.nio.charset.Charset
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object DeCryptMessages {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128

    fun generateSecretKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(256)
        return keyGen.generateKey()
    }

    fun encrypt(plainText: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        val gcmSpec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val encryptedText = cipher.doFinal(plainText.toByteArray(Charset.forName("UTF-8")))
        val encryptedTextWithIv = iv + encryptedText
        return Base64.encodeToString(encryptedTextWithIv, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String, secretKey: SecretKey): String {
        val encryptedTextWithIv = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = encryptedTextWithIv.sliceArray(0 until IV_SIZE)
        val encryptedBytes = encryptedTextWithIv.sliceArray(IV_SIZE until encryptedTextWithIv.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charset.forName("UTF-8"))
    }

    fun secretKeyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
    }

    fun stringToSecretKey(encodedKey: String): SecretKey {
        val decodedKey = Base64.decode(encodedKey, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, ALGORITHM)
    }
}