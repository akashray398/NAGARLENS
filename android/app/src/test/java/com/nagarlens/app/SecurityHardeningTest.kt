package com.nagarlens.app

import com.nagarlens.app.core.security.SecurityHardeningManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityHardeningTest {

    @Test
    fun `securityHardeningManager stores and clears session tokens`() {
        val manager = SecurityHardeningManager()
        manager.storeSessionTokens("access_token_123", "refresh_token_456")

        assertEquals("access_token_123", manager.getAccessToken())

        manager.clearSessionTokens()
        assertNull(manager.getAccessToken())
    }

    @Test
    fun `securityHardeningManager verifies SHA-256 media digest`() {
        val manager = SecurityHardeningManager()
        val data = "hello".toByteArray(Charsets.UTF_8)
        // Known SHA-256 for "hello"
        val expectedHash = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"

        assertTrue(manager.verifySha256Digest(data, expectedHash))
        assertFalse(manager.verifySha256Digest(data, "0000000000000000000000000000000000000000000000000000000000000000"))
    }
}
