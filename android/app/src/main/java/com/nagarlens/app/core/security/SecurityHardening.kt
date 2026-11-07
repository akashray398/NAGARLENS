package com.nagarlens.app.core.security

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityHardeningManager @Inject constructor() {

    private var activeAccessToken: String? = null
    private var activeRefreshToken: String? = null

    fun storeSessionTokens(accessToken: String, refreshToken: String) {
        this.activeAccessToken = accessToken
        this.activeRefreshToken = refreshToken
    }

    fun getAccessToken(): String? = activeAccessToken

    fun clearSessionTokens() {
        this.activeAccessToken = null
        this.activeRefreshToken = null
    }

    fun verifySha256Digest(dataBytes: ByteArray, expectedDigestHex: String): Boolean {
        if (dataBytes.isEmpty()) return false
        val md = MessageDigest.getInstance("SHA-256")
        val computedBytes = md.digest(dataBytes)
        val computedHex = computedBytes.joinToString("") { "%02x".format(it) }
        return computedHex.equals(expectedDigestHex, ignoreCase = true)
    }
}
