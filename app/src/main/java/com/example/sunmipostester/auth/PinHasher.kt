package com.example.sunmipostester.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PIN hashing for staff sign-in.
 *
 * SECURITY DESIGN
 *  - The PIN is never stored, logged, or exported — only a PBKDF2 derivation is.
 *  - PBKDF2-HMAC-SHA256 with a per-user random 16-byte salt and [ITERATIONS]
 *    rounds, so a leaked database cannot be reversed cheaply. A 4-6 digit PIN has
 *    a tiny keyspace, which is exactly why the KDF must be deliberately slow.
 *  - Verification compares digests in constant time to avoid leaking information
 *    through response timing.
 *  - Char arrays used for the PIN are wiped after derivation so the value does not
 *    linger in memory longer than needed.
 *
 * NOTE: a PIN is low-entropy by nature. This protects the stored credential, but
 * device-level protection (kiosk mode, physical control of the terminal) is still
 * required. Recommend an independent security review before production use.
 */
object PinHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    /** Minimum / maximum PIN length accepted at enrolment. */
    const val MIN_PIN_LENGTH = 4
    const val MAX_PIN_LENGTH = 8

    private val secureRandom = SecureRandom()

    /** A freshly derived credential: both parts must be stored together. */
    data class Credential(val hash: String, val salt: String)

    /**
     * Derive a new credential for [pin]. Generates a fresh random salt.
     * The caller should discard the plaintext PIN immediately afterwards.
     */
    fun create(pin: String): Credential {
        val salt = ByteArray(SALT_BYTES).also { secureRandom.nextBytes(it) }
        val hash = derive(pin, salt)
        return Credential(
            hash = Base64.encodeToString(hash, Base64.NO_WRAP),
            salt = Base64.encodeToString(salt, Base64.NO_WRAP)
        )
    }

    /**
     * Verify [pin] against a stored [hash]/[salt] pair.
     * Returns false on any decoding problem rather than throwing, so a corrupted
     * record simply denies access instead of crashing the till.
     */
    fun verify(pin: String, hash: String, salt: String): Boolean {
        return try {
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
            val expected = Base64.decode(hash, Base64.NO_WRAP)
            val actual = derive(pin, saltBytes)
            MessageDigest.isEqual(expected, actual) // constant-time comparison
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /** True when the PIN meets the basic policy (digits only, length bounds). */
    fun isPolicyValid(pin: String): Boolean =
        pin.length in MIN_PIN_LENGTH..MAX_PIN_LENGTH && pin.all { it.isDigit() }

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val chars = pin.toCharArray()
        try {
            return try {
                val spec = PBEKeySpec(chars, salt, ITERATIONS, KEY_LENGTH_BITS)
                SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
            } catch (e: Exception) {
                // Fallback for Android < API 26 where SecretKeyFactory PBKDF2WithHmacSHA256 is missing
                pbkdf2HmacSha256(chars, salt, ITERATIONS, KEY_LENGTH_BITS / 8)
            }
        } finally {
            // Wipe the sensitive material we control.
            chars.fill('\u0000')
        }
    }

    private fun pbkdf2HmacSha256(password: CharArray, salt: ByteArray, iterations: Int, dkLen: Int): ByteArray {
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        val passwordBytes = ByteArray(password.size) { i -> password[i].code.toByte() }
        try {
            mac.init(javax.crypto.spec.SecretKeySpec(passwordBytes, "HmacSHA256"))
            val hLen = 32
            val blocks = (dkLen + hLen - 1) / hLen
            val result = ByteArray(dkLen)
            var next = ByteArray(hLen)
            val accumulator = ByteArray(hLen)

            for (block in 1..blocks) {
                val saltBlock = salt + byteArrayOf(
                    (block shr 24).toByte(),
                    (block shr 16).toByte(),
                    (block shr 8).toByte(),
                    block.toByte()
                )
                var u = ByteArray(hLen)
                mac.update(saltBlock)
                mac.doFinal(u, 0)
                System.arraycopy(u, 0, accumulator, 0, hLen)

                // Reuse fixed buffers rather than allocating a new 32-byte array
                // for each of the 120,000 HMAC rounds on older Android runtimes.
                for (round in 2..iterations) {
                    mac.update(u)
                    mac.doFinal(next, 0)
                    for (index in 0 until hLen) {
                        accumulator[index] = (accumulator[index].toInt() xor next[index].toInt()).toByte()
                    }
                    val previous = u
                    u = next
                    next = previous
                }

                val offset = (block - 1) * hLen
                System.arraycopy(accumulator, 0, result, offset, minOf(hLen, dkLen - offset))
                u.fill(0)
                saltBlock.fill(0)
            }

            next.fill(0)
            accumulator.fill(0)
            return result
        } finally {
            passwordBytes.fill(0)
        }
    }
}
