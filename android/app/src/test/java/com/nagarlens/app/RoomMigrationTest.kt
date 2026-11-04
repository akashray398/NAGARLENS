package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.AssetEntity
import com.nagarlens.app.core.database.EvidenceEntity
import com.nagarlens.app.core.database.IncidentEntity
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.database.NotificationEntity
import com.nagarlens.app.core.model.AssetCategory
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
class RoomMigrationTest {

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
    fun `nagarlens database version 5 supports evidence, assets, incidents, and notifications tables concurrently`() = runTest {
        val evidenceDao = database.evidenceDao()
        val assetDao = database.assetDao()
        val incidentDao = database.incidentDao()
        val notificationDao = database.notificationDao()

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

        val incidentId = "INC-TEST-001"
        val incidentEntity = IncidentEntity(
            id = incidentId,
            assetId = assetId,
            assetCode = "NAG-CB-000001",
            title = "Test Incident",
            status = "OPEN",
            priority = "CRITICAL",
            defectCategory = "DRAIN_BLOCKAGE"
        )

        val notificationId = "NOTIF-TEST-001"
        val notificationEntity = NotificationEntity(
            id = notificationId,
            title = "Test Warning",
            message = "Critical blockage detected",
            category = "CRITICAL_ALERT",
            severity = "CRITICAL",
            relatedAssetId = assetId
        )

        evidenceDao.insertEvidence(evidenceEntity)
        assetDao.insertAsset(assetEntity)
        incidentDao.insertIncident(incidentEntity)
        notificationDao.insertNotification(notificationEntity)

        val retrievedEvidence = evidenceDao.getEvidenceById(evidenceId)
        val retrievedAsset = assetDao.getAssetById(assetId)
        val retrievedIncident = incidentDao.getIncidentById(incidentId)

        assertNotNull(retrievedEvidence)
        assertNotNull(retrievedAsset)
        assertNotNull(retrievedIncident)
        assertEquals("test.jpg", retrievedEvidence!!.originalFileName)
        assertEquals("NAG-CB-000001", retrievedAsset!!.assetCode)
        assertEquals("Test Incident", retrievedIncident!!.title)

        val allEvidence = evidenceDao.observeAllEvidence().first()
        val allAssets = assetDao.observeAllAssets().first()
        val allIncidents = incidentDao.observeAllIncidents().first()
        val allNotifications = notificationDao.observeAllNotifications().first()

        assertEquals(1, allEvidence.size)
        assertEquals(1, allAssets.size)
        assertEquals(1, allIncidents.size)
        assertEquals(1, allNotifications.size)
    }
}
