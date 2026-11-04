package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.AssetEntity
import com.nagarlens.app.core.database.InterventionDao
import com.nagarlens.app.core.database.InterventionEvidenceLinkDao
import com.nagarlens.app.core.database.InterventionEvidenceLinkEntity
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.database.ObservationDao
import com.nagarlens.app.core.database.ObservationEvidenceLinkDao
import com.nagarlens.app.core.database.ObservationEvidenceLinkEntity
import com.nagarlens.app.core.database.ObservationEntity
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.AssetLifecycleStatus
import com.nagarlens.app.core.model.ConditionSeverity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.InterventionEvidenceType
import com.nagarlens.app.core.model.ObservationType
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
class EvidenceRelationshipRetentionTest {

    private lateinit var database: NagarlensDatabase
    private lateinit var assetDao: AssetDao
    private lateinit var observationDao: ObservationDao
    private lateinit var obsLinkDao: ObservationEvidenceLinkDao
    private lateinit var intLinkDao: InterventionEvidenceLinkDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NagarlensDatabase::class.java
        ).allowMainThreadQueries().build()
        assetDao = database.assetDao()
        observationDao = database.observationDao()
        obsLinkDao = database.observationEvidenceLinkDao()
        intLinkDao = database.interventionEvidenceLinkDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `observation and intervention evidence links are persisted in Room`() = runTest {
        val obsLinkId = UUID.randomUUID().toString()
        val obsLink = ObservationEvidenceLinkEntity(
            id = obsLinkId,
            observationId = "obs_100",
            evidenceId = "evid_photo_01",
            relationshipType = "CONDITION_PROOF"
        )
        obsLinkDao.insertLink(obsLink)

        val retrievedObsLinks = obsLinkDao.getLinksForObservationSync("obs_100")
        assertEquals(1, retrievedObsLinks.size)
        assertEquals("evid_photo_01", retrievedObsLinks[0].evidenceId)

        val intLinkId = UUID.randomUUID().toString()
        val intLink = InterventionEvidenceLinkEntity(
            id = intLinkId,
            interventionId = "int_200",
            evidenceId = "evid_photo_02",
            relationshipType = InterventionEvidenceType.BEFORE_ACTION.name
        )
        intLinkDao.insertLink(intLink)

        val retrievedIntLinks = intLinkDao.getLinksForInterventionSync("int_200")
        assertEquals(1, retrievedIntLinks.size)
        assertEquals("evid_photo_02", retrievedIntLinks[0].evidenceId)
        assertEquals(InterventionEvidenceType.BEFORE_ACTION.name, retrievedIntLinks[0].relationshipType)
    }

    @Test
    fun `asset retirement preserves all historical observations and evidence links intact`() = runTest {
        val assetId = UUID.randomUUID().toString()
        val asset = AssetEntity(
            id = assetId,
            assetCode = "NAG-CB-000104",
            assetCategory = AssetCategory.CATCH_BASIN.name,
            lifecycleStatus = AssetLifecycleStatus.ACTIVE.name,
            latitude = 12.9716,
            longitude = 77.5946
        )
        assetDao.insertAsset(asset)

        val obs = ObservationEntity(
            id = "obs_hist_1",
            assetId = assetId,
            observationType = ObservationType.ROUTINE_INSPECTION.name,
            conditionCategory = DefectCategory.SOLID_WASTE.name,
            conditionSeverity = ConditionSeverity.MODERATE.name,
            observedAtUtc = 1000L,
            latitude = 12.9716,
            longitude = 77.5946
        )
        observationDao.insertObservation(obs)

        // Retire asset
        val retiredAsset = asset.copy(lifecycleStatus = AssetLifecycleStatus.RETIRED.name, version = 2)
        assetDao.updateAsset(retiredAsset)

        val updatedAssetInDb = assetDao.getAssetById(assetId)
        assertNotNull(updatedAssetInDb)
        assertEquals(AssetLifecycleStatus.RETIRED.name, updatedAssetInDb!!.lifecycleStatus)

        // Verify historical observation is 100% preserved
        val observations = observationDao.observeObservationsForAsset(assetId).first()
        assertEquals(1, observations.size)
        assertEquals("obs_hist_1", observations[0].id)
    }
}
