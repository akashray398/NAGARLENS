package com.nagarlens.app

import com.nagarlens.app.core.integrity.EvidenceFileStore
import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.model.MediaType
import com.nagarlens.app.core.repository.EvidenceRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class EvidenceRepositoryTest {

    @get:Rule
    val sourceFolder = TemporaryFolder()

    @get:Rule
    val targetFolder = TemporaryFolder()

    @Test
    fun `savePhotoEvidence preserves file, computes SHA-256 hash and emits domain entity`() = runTest {
        val tempPhoto = sourceFolder.newFile("temp_photo.jpg")
        tempPhoto.writeBytes("SAMPLE_CAMERA_PHOTO_BINARY_DATA".toByteArray())

        val sha256Hasher = Sha256Hasher()

        // Test implementation of EvidenceFileStore using TemporaryFolder
        val evidenceFileStore = object : EvidenceFileStore(dummyContext()) {
            override fun storeOriginalEvidence(sourceFile: File, targetFileName: String): File {
                val target = File(targetFolder.root, targetFileName)
                sourceFile.copyTo(target)
                return target
            }
        }

        val repository = EvidenceRepositoryImpl(evidenceFileStore, sha256Hasher)

        val locationData = LocationData(
            latitude = 12.9716,
            longitude = 77.5946,
            accuracyMeters = 1.8f,
            timestampUtc = System.currentTimeMillis()
        )

        val result = repository.savePhotoEvidence(tempPhoto, locationData).first()

        assertTrue(result.isSuccess)
        val evidence = result.getOrThrow()

        assertEquals(MediaType.PHOTO, evidence.mediaType)
        assertEquals(64, evidence.sha256Hash.length)
        assertEquals(12.9716, evidence.latitude!!, 0.0001)

        val allEvidence = repository.getAllEvidence().first()
        assertEquals(1, allEvidence.size)
        assertEquals(evidence.id, allEvidence[0].id)
    }

    private fun dummyContext(): android.content.Context {
        return object : android.content.ContextWrapper(null) {
            override fun getFilesDir(): File = targetFolder.root
        }
    }
}
