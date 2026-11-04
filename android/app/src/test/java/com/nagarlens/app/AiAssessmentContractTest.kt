package com.nagarlens.app

import com.nagarlens.app.core.model.AiAssessment
import com.nagarlens.app.core.model.BoundingBox
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.ImageQualityStatus
import com.nagarlens.app.core.model.ProcessingStatus
import com.nagarlens.app.core.model.TAXONOMY_VERSION
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.UUID

class AiAssessmentContractTest {

    @Test
    fun `ai assessment model retains contract attributes`() {
        val assessmentId = UUID.randomUUID().toString()
        val evidenceId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val bbox = BoundingBox(
            category = DefectCategory.SOLID_WASTE,
            confidence = 0.95f,
            xMin = 0.1f,
            yMin = 0.2f,
            xMax = 0.5f,
            yMax = 0.6f
        )

        val assessment = AiAssessment(
            assessmentId = assessmentId,
            evidenceId = evidenceId,
            modelName = "MobileNetV3-Small-DrainDefect",
            modelVersion = "v1.0.0-foundation",
            taxonomyVersion = TAXONOMY_VERSION,
            predictedCategory = DefectCategory.SOLID_WASTE,
            confidenceScore = 0.95f,
            boundingBoxes = listOf(bbox),
            imageQualityStatus = ImageQualityStatus.PASS,
            processingStatus = ProcessingStatus.COMPLETED,
            message = "Defect detected.",
            inferenceTimestampUtc = now
        )

        assertEquals(assessmentId, assessment.assessmentId)
        assertEquals(evidenceId, assessment.evidenceId)
        assertEquals(DefectCategory.SOLID_WASTE, assessment.predictedCategory)
        assertEquals(0.95f, assessment.confidenceScore, 0.001f)
        assertEquals(1, assessment.boundingBoxes.size)
        assertEquals(ProcessingStatus.COMPLETED, assessment.processingStatus)
    }
}
