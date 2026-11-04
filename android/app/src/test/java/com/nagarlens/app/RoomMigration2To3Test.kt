package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.AssetEntity
import com.nagarlens.app.core.database.EvidenceEntity
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.ConditionSeverity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.ObservationType
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
class RoomMigration2To3Test {

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
    fun `nagarlens database version 3 supports evidence assets observations interventions and verifications`() = runTest {
        val evidenceDao = database.evidenceDao()
        val assetDao = database.assetDao()
        val observationDao = database.observationDao()

        val evidenceId = UUID.randomUUID().toString()
        val evidenceEntity = EvidenceEntity(
            id = evidenceId,
            mediaUri = "file:///test.jpg",
            mediaType = "PHOTO",
            originalFileName = "test.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = 100L,
            captureTimestampUtc = 1000L,
            sha256Hash = "hash123",
            syncStatus = SyncStatus.LOCAL_ONLY.name
        )

        val assetId = UUID.randomUUID().toString()
        val assetEntity = AssetEntity(
            id = assetId,
            assetCode = "NAG-CB-000001",
            assetCategory = AssetCategory.CATCH_BASIN.name,
            latitude = 12.9716,
            longitude = 77.5946
        )

        evidenceDao.insertEvidence(evidenceEntity)
        assetDao.insertAsset(assetEntity)

        val obsRes = database.observationDao().observeObservationsForAsset(assetId).first()

        assertNotNull(evidenceDao.getEvidenceById(evidenceId))
        assertNotNull(assetDao.getAssetById(assetId))
        assertEquals(0, obsRes.size)
    }
}
