package com.nagarlens.app.core.integrity

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Sha256Hasher @Inject constructor() {

    fun calculateFileSha256(file: File): String {
        require(file.exists()) { "Target media file does not exist: ${file.absolutePath}" }
        require(file.length() > 0) { "Target media file is empty: ${file.absolutePath}" }

        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)

        FileInputStream(file).use { inputStream ->
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun verifyFileHash(file: File, expectedHash: String): Boolean {
        if (!file.exists() || file.length() == 0L) return false
        val computedHash = calculateFileSha256(file)
        return computedHash.equals(expectedHash, ignoreCase = true)
    }
}
