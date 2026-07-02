package com.example.train_ticket_booking_system.util

import android.util.Log
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHelper {
    private const val TAG = "TTBS_AUTH"
    private const val PREFIX = "PBKDF2"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    private val random = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH).also { random.nextBytes(it) }
        val hash = pbkdf2(password, salt, ITERATIONS)
        Log.d(TAG, "hash: generated PBKDF2 hash, iterations=$ITERATIONS")
        return "$PREFIX:$ITERATIONS:${salt.toHex()}:${hash.toHex()}"
    }

    fun verify(password: String, stored: String): Boolean {
        if (stored.isEmpty() || !stored.startsWith("$PREFIX:")) {
            Log.d(TAG, "verify: stored value empty or not PBKDF2 format, rejecting")
            return false
        }
        return try {
            val parts = stored.split(":")
            if (parts.size != 4) return false
            val iterations = parts[1].toInt()
            val salt = parts[2].hexToBytes()
            val expected = parts[3].hexToBytes()
            val actual = pbkdf2(password, salt, iterations)
            val match = constantTimeEquals(expected, actual)
            Log.d(TAG, "verify: PBKDF2 verification result=$match")
            match
        } catch (e: Exception) {
            Log.e(TAG, "verify: error parsing stored hash", e)
            false
        }
    }

    private fun pbkdf2(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } catch (e: NoSuchAlgorithmException) {
            Log.w(TAG, "PBKDF2WithHmacSHA256 unavailable, falling back to PBKDF2WithHmacSHA1")
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
