package com.nagarlens.app

import com.nagarlens.app.core.integrity.Sha256Hasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class Sha256HasherTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val sha256Hasher = Sha256Hasher()

    @Test
    fun `sha256 calculation produces consistent hash for same file content`() {
        val testFile = tempFolder.newFile("test_media.jpg")
        testFile.writeBytes(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05))

        val hash1 = sha256Hasher.calculateFileSha256(testFile)
        val hash2 = sha256Hasher.calculateFileSha256(testFile)

        assertEquals(64, hash1.length)
        assertEquals(hash1, hash2)
    }

    @Test
    fun `modifying file content invalidates sha256 hash digest`() {
        val testFile = tempFolder.newFile("test_media_tampered.jpg")
        testFile.writeBytes(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05))
        val originalHash = sha256Hasher.calculateFileSha256(testFile)

        // Mutate a single byte in the media binary
        testFile.writeBytes(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x99.toByte()))
        val mutatedHash = sha256Hasher.calculateFileSha256(testFile)

        assertNotEquals(originalHash, mutatedHash)
    }

    @Test
    fun `verifyFileHash returns true for matching content and false for tampered content`() {
        val testFile = tempFolder.newFile("verify_hash.mp4")
        testFile.writeBytes("NAGARLENS_ORIGINAL_EVIDENCE_STREAM".toByteArray())

        val validHash = sha256Hasher.calculateFileSha256(testFile)
        assertTrue(sha256Hasher.verifyFileHash(testFile, validHash))
        assertFalse(sha256Hasher.verifyFileHash(testFile, "0000000000000000000000000000000000000000000000000000000000000000"))
    }
}
