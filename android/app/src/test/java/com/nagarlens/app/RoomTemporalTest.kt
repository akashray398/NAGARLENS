package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.InterventionDao
import com.nagarlens.app.core.database.InterventionEntity
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.database.ObservationDao
import com.nagarlens.app.core.database.ObservationEntity
import com.nagarlens.app.core.database.OutcomeVerificationDao
import com.nagarlens.app.core.database.OutcomeVerificationEntity
import com.nagarlens.app.core.model.ConditionSeverity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.InterventionStatus
import com.nagarlens.app.core.model.InterventionType
import com.nagarlens.app.core.model.ObservationType
import com.nagarlens.app.core.model.VerificationOutcome
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
class RoomTemporalTest {

    private lateinit var database: NagarlensDatabase
    private lateinit var observationDao: ObservationDao
    private lateinit var interventionDao: InterventionDao
    private lateinit var outcomeVerificationDao: OutcomeVerificationDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NagarlensDatabase::class.java
        ).allowMainThreadQueries().build()
        observationDao = database.observationDao()
        interventionDao = database.interventionDao()
        outcomeVerificationDao = database.outcomeVerificationDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `observationDao inserts and observes observations ordered by observedAtUtc`() = runTest {
        val assetId = UUID.randomUUID().toString()
        val obs1 = ObservationEntity(
            id = "obs_1",
            assetId = assetId,
            observationType = ObservationType.INITIAL_INSPECTION.name,
            conditionCategory = DefectCategory.SOLID_WASTE.name,
            conditionSeverity = ConditionSeverity.MODERATE.name,
            observedAtUtc = 1000L,
            latitude = 12.9716,
            longitude = 77.5946
        )

        val obs2 = ObservationEntity(
            id = "obs_2",
            assetId = assetId,
            observationType = ObservationType.BLOCKAGE_REPORT.name,
            conditionCategory = DefectCategory.DRAIN_BLOCKAGE.name,
            conditionSeverity = ConditionSeverity.CRITICAL.name,
            observedAtUtc = 2000L,
            latitude = 12.9716,
            longitude = 77.5946
        )

        observationDao.insertObservation(obs2)
        observationDao.insertObservation(obs1)

        val list = observationDao.observeObservationsForAsset(assetId).first()
        assertEquals(2, list.size)
        assertEquals("obs_1", list[0].id)  // Oldest first
        assertEquals("obs_2", list[1].id)
    }

    @Test
    fun `interventionDao inserts and observes interventions for asset`() = runTest {
        val assetId = UUID.randomUUID().toString()
        val interv = InterventionEntity(
            id = "int_1",
            assetId = assetId,
            interventionType = InterventionType.SILT_REMOVAL.name,
            title = "Desilting catch basin",
            status = InterventionStatus.PROPOSED.name
        )

        interventionDao.insertIntervention(interv)

        val list = interventionDao.observeInterventionsForAsset(assetId).first()
        assertEquals(1, list.size)
        assertEquals("int_1", list[0].id)
        assertEquals("Desilting catch basin", list[0].title)
    }

    @Test
    fun `outcomeVerificationDao inserts and observes outcome verifications`() = runTest {
        val assetId = UUID.randomUUID().toString()
        val ver = OutcomeVerificationEntity(
            id = "ver_1",
            interventionId = "int_1",
            assetId = assetId,
            verificationObservationId = "obs_2",
            outcome = VerificationOutcome.IMPROVED.name,
            verifiedAtUtc = 3000L
        )

        outcomeVerificationDao.insertOutcomeVerification(ver)

        val list = outcomeVerificationDao.observeOutcomeVerificationsForAsset(assetId).first()
        assertEquals(1, list.size)
        assertEquals("ver_1", list[0].id)
        assertEquals(VerificationOutcome.IMPROVED.name, list[0].outcome)
    }
}
