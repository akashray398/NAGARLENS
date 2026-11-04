package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.integrity.EvidenceFileStore
import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.model.MediaType
import com.nagarlens.app.core.repository.EvidenceRepositoryImpl
import com.nagarlens.app.core.sync.EvidenceSyncScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EvidenceRepositoryTest {

    @get:Rule
    val sourceFolder = TemporaryFolder()

    @get:Rule
    val targetFolder = TemporaryFolder()

    private lateinit var database: NagarlensDatabase

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NagarlensDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `savePhotoEvidence preserves file, computes SHA-256 hash and emits domain entity`() = runTest {
        val tempPhoto = sourceFolder.newFile("temp_photo.jpg")
        tempPhoto.writeBytes("SAMPLE_CAMERA_PHOTO_BINARY_DATA".toByteArray())

        val sha256Hasher = Sha256Hasher()

        val evidenceFileStore = object : EvidenceFileStore(ApplicationProvider.getApplicationContext()) {
            override fun storeOriginalEvidence(sourceFile: File, targetFileName: String): File {
                val target = File(targetFolder.root, targetFileName)
                sourceFile.copyTo(target)
                return target
            }

            override fun getEvidenceFile(fileName: String): File? {
                val file = File(targetFolder.root, fileName)
                return if (file.exists()) file else null
            }
        }

        val noOpScheduler = object : EvidenceSyncScheduler(ApplicationProvider.getApplicationContext()) {
            override fun scheduleSync(evidenceId: String) {}
        }

        val repository = EvidenceRepositoryImpl(
            evidenceDao = database.evidenceDao(),
            evidenceFileStore = evidenceFileStore,
            sha256Hasher = sha256Hasher,
            evidenceSyncScheduler = noOpScheduler
        )

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
}
