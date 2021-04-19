package com.example.vaultapplication.encryption

import android.util.Base64
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object SiteEncryption {

    private const val pswdIterations = 10
    private const val keySize = 128
    private const val cypherInstance = "AES/CBC/PKCS5Padding"
    private const val secretKeyInstance = "PBKDF2WithHmacSHA1"
    private const val plainText = "com.example.vaultpplication.encryption.siteplain"
    private const val AESSalt = "com.example.vaultpplication.encryption.sitesalt"
    private const val initializationVector = "5235893145604157"

    @Throws(Exception::class)
    fun encrypt(textToEncrypt: String): String? {
        val skeySpec = SecretKeySpec(getRaw(plainText, AESSalt), "AES")
        val cipher = Cipher.getInstance(cypherInstance)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            skeySpec,
            IvParameterSpec(initializationVector.toByteArray())
        )
        val encrypted = cipher.doFinal(textToEncrypt.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun decrypt(textToDecrypt: String?): String? {
        val encryted_bytes: ByteArray = Base64.decode(textToDecrypt, Base64.DEFAULT)
        val skeySpec = SecretKeySpec(getRaw(plainText, AESSalt), "AES")
        val cipher = Cipher.getInstance(this.cypherInstance)
        cipher.init(
            Cipher.DECRYPT_MODE,
            skeySpec,
            IvParameterSpec(initializationVector.toByteArray())
        )
        val decrypted = cipher.doFinal(encryted_bytes)
        return String(decrypted, charset("UTF-8"))
    }

    private fun getRaw(plainText: String, salt: String): ByteArray? {
        try {
            val factory = SecretKeyFactory.getInstance(secretKeyInstance)
            val spec: KeySpec =
                PBEKeySpec(plainText.toCharArray(), salt.toByteArray(), pswdIterations, keySize)
            return factory.generateSecret(spec).encoded
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }
}