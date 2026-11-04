package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.InterventionDao
import com.nagarlens.app.core.database.InterventionEvidenceLinkDao
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.database.ObservationDao
import com.nagarlens.app.core.database.ObservationEvidenceLinkDao
import com.nagarlens.app.core.database.OutcomeVerificationDao
import com.nagarlens.app.core.model.ConditionSeverity
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.InterventionType
import com.nagarlens.app.core.model.ObservationType
import com.nagarlens.app.core.model.VerificationOutcome
import com.nagarlens.app.core.repository.TemporalRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TemporalRepositoryTest {

    private lateinit var database: NagarlensDatabase
    private lateinit var observationDao: ObservationDao
    private lateinit var observationEvidenceLinkDao: ObservationEvidenceLinkDao
    private lateinit var interventionDao: InterventionDao
    private lateinit var interventionEvidenceLinkDao: InterventionEvidenceLinkDao
    private lateinit var outcomeVerificationDao: OutcomeVerificationDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NagarlensDatabase::class.java
        ).allowMainThreadQueries().build()
        observationDao = database.observationDao()
        observationEvidenceLinkDao = database.observationEvidenceLinkDao()
        interventionDao = database.interventionDao()
        interventionEvidenceLinkDao = database.interventionEvidenceLinkDao()
        outcomeVerificationDao = database.outcomeVerificationDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `recordObservation and getAssetTimeline compiles chronological event memory graph`() = runTest {
        val repository = TemporalRepositoryImpl(
            observationDao,
            observationEvidenceLinkDao,
            interventionDao,
            interventionEvidenceLinkDao,
            outcomeVerificationDao
        )
        val assetId = UUID.randomUUID().toString()

        val obsRes = repository.recordObservation(
            assetId = assetId,
            type = ObservationType.BLOCKAGE_REPORT,
            category = DefectCategory.SOLID_WASTE,
            severity = ConditionSeverity.HIGH,
            description = "Solid waste blocking drain opening",
            latitude = 12.9716,
            longitude = 77.5946
        )
        assertTrue(obsRes.isSuccess)
        val obs = obsRes.getOrThrow()

        val intRes = repository.recordIntervention(
            assetId = assetId,
            type = InterventionType.SOLID_WASTE_REMOVAL,
            title = "Trash Removal",
            description = "Cleared plastic bottles and bags"
        )
        assertTrue(intRes.isSuccess)
        val interv = intRes.getOrThrow()

        val verRes = repository.submitOutcomeVerification(
            interventionId = interv.id,
            assetId = assetId,
            verificationObservationId = obs.id,
            outcome = VerificationOutcome.IMPROVED,
            description = "Flow restored completely"
        )
        assertTrue(verRes.isSuccess)

        val timeline = repository.getAssetTimeline(assetId, "NAG-CB-000104").first()
        assertEquals(3, timeline.totalEvents)
        assertEquals("NAG-CB-000104", timeline.assetCode)
        assertEquals("OBSERVATION", timeline.events[0].eventType)
        assertEquals("INTERVENTION", timeline.events[1].eventType)
        assertEquals("VERIFICATION", timeline.events[2].eventType)
    }
}
