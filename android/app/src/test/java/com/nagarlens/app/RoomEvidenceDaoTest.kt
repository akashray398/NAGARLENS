package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.EvidenceDao
import com.nagarlens.app.core.database.EvidenceEntity
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomEvidenceDaoTest {

    private lateinit var database: NagarlensDatabase
    private lateinit var evidenceDao: EvidenceDao

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
    fun `insertEvidence persists record and observes via Flow`() = runTest {
        val id = UUID.randomUUID().toString()
        val entity = EvidenceEntity(
            id = id,
            mediaUri = "file:///data/user/0/com.nagarlens.app/files/evidence/EVID_PHOTO_$id.jpg",
            mediaType = "PHOTO",
            originalFileName = "EVID_PHOTO_$id.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = 2048L,
            captureTimestampUtc = System.currentTimeMillis(),
            sha256Hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            syncStatus = SyncStatus.LOCAL_ONLY.name
        )

        evidenceDao.insertEvidence(entity)

        val retrieved = evidenceDao.getEvidenceById(id)
        assertNotNull(retrieved)
        assertEquals(id, retrieved!!.id)

        val allRecords = evidenceDao.observeAllEvidence().first()
        assertEquals(1, allRecords.size)
        assertEquals(id, allRecords[0].id)
    }

    @Test
    fun `duplicate UUID insertion is ignored via OnConflictStrategy IGNORE`() = runTest {
        val id = UUID.randomUUID().toString()
        val entity1 = EvidenceEntity(
            id = id,
            mediaUri = "file:///data/user/0/com.nagarlens.app/files/evidence/EVID_PHOTO_$id.jpg",
            mediaType = "PHOTO",
            originalFileName = "EVID_PHOTO_$id.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = 2048L,
            captureTimestampUtc = 1000L,
            sha256Hash = "hash1",
            syncStatus = SyncStatus.LOCAL_ONLY.name
        )

        val entity2 = entity1.copy(fileSizeBytes = 9999L, sha256Hash = "hash2")

        evidenceDao.insertEvidence(entity1)
        evidenceDao.insertEvidence(entity2)

        val record = evidenceDao.getEvidenceById(id)
        assertNotNull(record)
        assertEquals(2048L, record!!.fileSizeBytes)
        assertEquals("hash1", record.sha256Hash)
    }

    @Test
    fun `observeEvidenceBySyncStatus filters records accurately`() = runTest {
        val id1 = UUID.randomUUID().toString()
        val id2 = UUID.randomUUID().toString()

        val entity1 = EvidenceEntity(
            id = id1,
            mediaUri = "uri1",
            mediaType = "PHOTO",
            originalFileName = "file1",
            mimeType = "image/jpeg",
            fileSizeBytes = 100L,
            captureTimestampUtc = 1000L,
            sha256Hash = "hash1",
            syncStatus = SyncStatus.LOCAL_ONLY.name
        )

        val entity2 = EvidenceEntity(
            id = id2,
            mediaUri = "uri2",
            mediaType = "VIDEO",
            originalFileName = "file2",
            mimeType = "video/mp4",
            fileSizeBytes = 200L,
            captureTimestampUtc = 2000L,
            sha256Hash = "hash2",
            syncStatus = SyncStatus.QUEUED.name
        )

        evidenceDao.insertEvidence(entity1)
        evidenceDao.insertEvidence(entity2)

        val localOnly = evidenceDao.observeEvidenceBySyncStatus(SyncStatus.LOCAL_ONLY.name).first()
        assertEquals(1, localOnly.size)
        assertEquals(id1, localOnly[0].id)

        val queued = evidenceDao.observeEvidenceBySyncStatus(SyncStatus.QUEUED.name).first()
        assertEquals(1, queued.size)
        assertEquals(id2, queued[0].id)
    }

    @Test
    fun `updateSyncStatus and recordSyncFailure update database attributes consistently`() = runTest {
        val id = UUID.randomUUID().toString()
        val entity = EvidenceEntity(
            id = id,
            mediaUri = "uri1",
            mediaType = "PHOTO",
            originalFileName = "file1",
            mimeType = "image/jpeg",
            fileSizeBytes = 100L,
            captureTimestampUtc = 1000L,
            sha256Hash = "hash1",
            syncStatus = SyncStatus.QUEUED.name
        )

        evidenceDao.insertEvidence(entity)

        evidenceDao.recordSyncFailure(
            id = id,
            syncStatus = SyncStatus.RETRYABLE_FAILURE.name,
            error = "Backend upload endpoint is not configured in current milestone.",
            updatedAt = 3000L
        )

        val updated = evidenceDao.getEvidenceById(id)
        assertNotNull(updated)
        assertEquals(SyncStatus.RETRYABLE_FAILURE.name, updated!!.syncStatus)
        assertEquals(1, updated.retryCount)
        assertEquals("Backend upload endpoint is not configured in current milestone.", updated.lastSyncError)
    }
}
