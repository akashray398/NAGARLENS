package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.EvidenceDao
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.integrity.EvidenceFileStore
import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.model.MediaType
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.repository.EvidenceRepositoryImpl
import com.nagarlens.app.core.sync.EvidenceSyncScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
class EvidenceRepositoryRoomTest {

    @get:Rule
    val sourceFolder = TemporaryFolder()

    @get:Rule
    val targetFolder = TemporaryFolder()

    private lateinit var database: NagarlensDatabase
    private lateinit var evidenceDao: EvidenceDao
    private val sha256Hasher = Sha256Hasher()

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NagarlensDatabase::class.java
        ).allowMainThreadQueries().build()
        evidenceDao = database.evidenceDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `savePhotoEvidence persists in Room, computes SHA-256 hash and sets QUEUED status`() = runTest {
        val tempPhoto = sourceFolder.newFile("temp_photo.jpg")
        tempPhoto.writeBytes("REAL_CAMERA_PHOTO_BINARY_BYTES".toByteArray())

        val evidenceFileStore = object : EvidenceFileStore(dummyContext()) {
            override fun storeOriginalEvidence(sourceFile: File, targetFileName: String): File {
                val target = File(targetFolder.root, targetFileName)
                sourceFile.copyTo(target)
                return target
            }

            override fun getEvidenceFile(fileName: String): File? {
                val file = File(targetFolder.root, fileName)
                return if (file.exists()) file else File(targetFolder.root, "dummy.jpg")
            }
        }

        val noOpScheduler = object : EvidenceSyncScheduler(dummyContext()) {
            override fun scheduleSync(evidenceId: String) {}
        }

        val repository = EvidenceRepositoryImpl(
            evidenceDao = evidenceDao,
            evidenceFileStore = evidenceFileStore,
            sha256Hasher = sha256Hasher,
            evidenceSyncScheduler = noOpScheduler
        )

        val locationData = LocationData(12.9716, 77.5946, 1.5f, System.currentTimeMillis())

        val result = repository.savePhotoEvidence(tempPhoto, locationData).first()

        assertTrue(result.isSuccess)
        val evidence = result.getOrThrow()

        assertEquals(MediaType.PHOTO, evidence.mediaType)
        assertEquals(SyncStatus.QUEUED, evidence.syncStatus)
        assertEquals(64, evidence.sha256Hash.length)

        val allInRoom = repository.getAllEvidence().first()
        assertEquals(1, allInRoom.size)
        assertEquals(evidence.id, allInRoom[0].id)
    }

    @Test
    fun `auditAndRecoverOrphanFiles discovers unindexed media files and recovers metadata into Room`() = runTest {
        val orphanFile = File(targetFolder.root, "EVID_PHOTO_orphan123.jpg")
        orphanFile.writeBytes("ORPHAN_MEDIA_BINARY_BYTES".toByteArray())

        val evidenceFileStore = object : EvidenceFileStore(dummyContext()) {
            override fun getEvidenceFile(fileName: String): File {
                return File(targetFolder.root, fileName)
            }
        }

        val noOpScheduler = object : EvidenceSyncScheduler(dummyContext()) {
            override fun scheduleSync(evidenceId: String) {}
        }

        val repository = EvidenceRepositoryImpl(
            evidenceDao = evidenceDao,
            evidenceFileStore = evidenceFileStore,
            sha256Hasher = sha256Hasher,
            evidenceSyncScheduler = noOpScheduler
        )

        val recoveredCount = repository.auditAndRecoverOrphanFiles()
        assertEquals(1, recoveredCount)

        val recoveredEvidence = repository.getEvidenceById("orphan123").first()
        assertNotNull(recoveredEvidence)
        assertEquals("EVID_PHOTO_orphan123.jpg", recoveredEvidence!!.originalFileName)
        assertEquals(SyncStatus.LOCAL_ONLY, recoveredEvidence.syncStatus)
    }

    private fun dummyContext(): android.content.Context {
        return ApplicationProvider.getApplicationContext()
    }
}
