package com.nagarlens.app

import com.nagarlens.app.core.model.InvestigationStatus
import com.nagarlens.app.core.repository.InvestigationRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InvestigationEngineTest {

    @Test
    fun `investigationRepository creates case and advances status`() = runTest {
        val repository = InvestigationRepositoryImpl()

        val createResult = repository.createInvestigation(
            title = "Testing Drain Line Investigation",
            assetId = "AST-999",
            assetCode = "NAG-CB-000999",
            description = "Test description"
        )
        assertTrue(createResult.isSuccess)
        val createdCase = createResult.getOrNull()
        assertNotNull(createdCase)
        assertEquals(InvestigationStatus.OPEN, createdCase!!.status)

        val advanceResult = repository.updateInvestigationStatus(
            id = createdCase.id,
            targetStatus = InvestigationStatus.INVESTIGATING,
            comment = "Advancing to investigation state."
        )
        assertTrue(advanceResult.isSuccess)
        val updatedCase = advanceResult.getOrNull()
        assertEquals(InvestigationStatus.INVESTIGATING, updatedCase!!.status)
        assertEquals(2, updatedCase.timelineEvents.size)
    }

    @Test
    fun `investigationRepository issues evidence request`() = runTest {
        val repository = InvestigationRepositoryImpl()

        val cases = repository.observeInvestigations().first()
        val initialCase = cases[0]

        val reqResult = repository.requestEvidence(
            investigationId = initialCase.id,
            assetId = initialCase.assetId,
            evidenceType = "PHOTO_CLOSEUP_INVERT",
            instructions = "Take close-up of grate invert."
        )
        assertTrue(reqResult.isSuccess)
        val req = reqResult.getOrNull()
        assertNotNull(req)
        assertEquals("PHOTO_CLOSEUP_INVERT", req!!.requestedEvidenceType)
        assertEquals("PENDING", req.status)
    }
}
