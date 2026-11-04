package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.EvidenceDao
import com.nagarlens.app.core.database.EvidenceEntity
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.integrity.EvidenceFileStore
import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.repository.EvidenceRepositoryImpl
import com.nagarlens.app.core.sync.EvidenceSyncScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StaleUploadingRecoveryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

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
    fun `stale UPLOADING record is recovered to RETRYABLE_FAILURE when media file and SHA-256 hash are intact`() = runTest {
        val file = tempFolder.newFile("EVID_PHOTO_stale1.jpg")
        file.writeBytes("INTACT_MEDIA_BINARY_STREAM".toByteArray())
        val hash = sha256Hasher.calculateFileSha256(file)

        val staleTimestamp = System.currentTimeMillis() - 60_000L // 60 seconds ago
        val entity = EvidenceEntity(
            id = "stale1",
            mediaUri = file.toURI().toString(),
            mediaType = "PHOTO",
            originalFileName = file.name,
            mimeType = "image/jpeg",
            fileSizeBytes = file.length(),
            captureTimestampUtc = staleTimestamp,
            sha256Hash = hash,
            syncStatus = SyncStatus.UPLOADING.name,
            updatedTimestampUtc = staleTimestamp
        )

        evidenceDao.insertEvidence(entity)

        val fileStore = object : EvidenceFileStore(ApplicationProvider.getApplicationContext()) {
            override fun getEvidenceFile(fileName: String): File? = if (fileName == file.name) file else null
        }
        val scheduler = object : EvidenceSyncScheduler(ApplicationProvider.getApplicationContext()) {}

        val repository = EvidenceRepositoryImpl(evidenceDao, fileStore, sha256Hasher, scheduler)

        val recoveredCount = repository.recoverStaleUploadingRecords(staleThresholdMs = 30_000L)
        assertEquals(1, recoveredCount)

        val recovered = evidenceDao.getEvidenceById("stale1")
        assertNotNull(recovered)
        assertEquals(SyncStatus.RETRYABLE_FAILURE.name, recovered!!.syncStatus)
        assertTrue(recovered.lastSyncError!!.contains("interrupted"))
        assertNotEquals(SyncStatus.SYNCED.name, recovered.syncStatus)
    }

    @Test
    fun `stale UPLOADING record transitions to PERMANENT_FAILURE if media file is missing`() = runTest {
        val staleTimestamp = System.currentTimeMillis() - 60_000L
        val entity = EvidenceEntity(
            id = "missing_file_id",
            mediaUri = "file:///non_existent_file.jpg",
            mediaType = "PHOTO",
            originalFileName = "non_existent_file.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = 500L,
            captureTimestampUtc = staleTimestamp,
            sha256Hash = "some_hash",
            syncStatus = SyncStatus.UPLOADING.name,
            updatedTimestampUtc = staleTimestamp
        )

        evidenceDao.insertEvidence(entity)

        val fileStore = object : EvidenceFileStore(ApplicationProvider.getApplicationContext()) {
            override fun getEvidenceFile(fileName: String): File? = null
        }
        val scheduler = object : EvidenceSyncScheduler(ApplicationProvider.getApplicationContext()) {}

        val repository = EvidenceRepositoryImpl(evidenceDao, fileStore, sha256Hasher, scheduler)

        val recoveredCount = repository.recoverStaleUploadingRecords(staleThresholdMs = 30_000L)
        assertEquals(1, recoveredCount)

        val recovered = evidenceDao.getEvidenceById("missing_file_id")
        assertNotNull(recovered)
        assertEquals(SyncStatus.PERMANENT_FAILURE.name, recovered!!.syncStatus)
    }

    @Test
    fun `stale UPLOADING record transitions to PERMANENT_FAILURE if media file SHA-256 hash is tampered`() = runTest {
        val file = tempFolder.newFile("EVID_PHOTO_tampered.jpg")
        file.writeBytes("MUTATED_BYTES".toByteArray())

        val staleTimestamp = System.currentTimeMillis() - 60_000L
        val entity = EvidenceEntity(
            id = "tampered_id",
            mediaUri = file.toURI().toString(),
            mediaType = "PHOTO",
            originalFileName = file.name,
            mimeType = "image/jpeg",
            fileSizeBytes = file.length(),
            captureTimestampUtc = staleTimestamp,
            sha256Hash = "ORIGINAL_UNMUTATED_HASH_DIGEST",
            syncStatus = SyncStatus.UPLOADING.name,
            updatedTimestampUtc = staleTimestamp
        )

        evidenceDao.insertEvidence(entity)

        val fileStore = object : EvidenceFileStore(ApplicationProvider.getApplicationContext()) {
            override fun getEvidenceFile(fileName: String): File? = file
        }
        val scheduler = object : EvidenceSyncScheduler(ApplicationProvider.getApplicationContext()) {}

        val repository = EvidenceRepositoryImpl(evidenceDao, fileStore, sha256Hasher, scheduler)

        val recoveredCount = repository.recoverStaleUploadingRecords(staleThresholdMs = 30_000L)
        assertEquals(1, recoveredCount)

        val recovered = evidenceDao.getEvidenceById("tampered_id")
        assertNotNull(recovered)
        assertEquals(SyncStatus.PERMANENT_FAILURE.name, recovered!!.syncStatus)
        assertTrue(recovered.lastSyncError!!.contains("hash integrity check failed"))
    }

    @Test
    fun `stale recovery is idempotent and leaves non-stale records untouched`() = runTest {
        val recentTimestamp = System.currentTimeMillis() - 1000L // 1 second ago (actively running worker)
        val entity = EvidenceEntity(
            id = "active_id",
            mediaUri = "file:///active.jpg",
            mediaType = "PHOTO",
            originalFileName = "active.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = 100L,
            captureTimestampUtc = recentTimestamp,
            sha256Hash = "active_hash",
            syncStatus = SyncStatus.UPLOADING.name,
            updatedTimestampUtc = recentTimestamp
        )

        evidenceDao.insertEvidence(entity)

        val fileStore = object : EvidenceFileStore(ApplicationProvider.getApplicationContext()) {}
        val scheduler = object : EvidenceSyncScheduler(ApplicationProvider.getApplicationContext()) {}

        val repository = EvidenceRepositoryImpl(evidenceDao, fileStore, sha256Hasher, scheduler)

        val recoveredCount = repository.recoverStaleUploadingRecords(staleThresholdMs = 30_000L)
        assertEquals(0, recoveredCount)

        val activeRecord = evidenceDao.getEvidenceById("active_id")
        assertEquals(SyncStatus.UPLOADING.name, activeRecord!!.syncStatus)
    }
}
