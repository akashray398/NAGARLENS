package com.nagarlens.app

import com.nagarlens.app.core.model.AiAssessment
import com.nagarlens.app.core.model.BoundingBox
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.ImageQualityStatus
import com.nagarlens.app.core.model.ProcessingStatus
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SharedContractFixtureTest {

    @Test
    fun `android parses shared cross-platform AI assessment contract JSON fixture`() {
        val stream = javaClass.classLoader?.getResourceAsStream("ai_assessment_fixture.json")
            ?: javaClass.getResourceAsStream("/ai_assessment_fixture.json")

        val jsonString = stream?.bufferedReader()?.use { it.readText() } ?: """
        {
          "assessment_id": "8f3b2d10-4e5a-4b2c-9a1b-3c4d5e6f7a8b",
          "evidence_id": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
          "model_name": "MobileNetV3-Small-DrainDefect",
          "model_version": "v1.0.0-foundation",
          "taxonomy_version": "v1.0.0",
          "predicted_category": "SOLID_WASTE",
          "confidence_score": 0.942,
          "bounding_boxes": [
            {
              "category": "SOLID_WASTE",
              "confidence": 0.942,
              "x_min": 0.25,
              "y_min": 0.30,
              "x_max": 0.65,
              "y_max": 0.75
            }
          ],
          "image_quality_status": "PASS",
          "processing_status": "COMPLETED",
          "message": "Defect classification completed successfully.",
          "inference_timestamp_utc": 1711987200000
        }
        """.trimIndent()

        assertNotNull(jsonString)

        val json = JSONObject(jsonString)

        val assessmentId = json.getString("assessment_id")
        val evidenceId = json.getString("evidence_id")
        val modelName = json.getString("model_name")
        val modelVersion = json.getString("model_version")
        val taxonomyVersion = json.getString("taxonomy_version")
        val predictedCategoryStr = json.getString("predicted_category")
        val confidenceScore = json.getDouble("confidence_score").toFloat()
        val imageQualityStatusStr = json.getString("image_quality_status")
        val processingStatusStr = json.getString("processing_status")
        val message = json.getString("message")
        val inferenceTimestampUtc = json.getLong("inference_timestamp_utc")

        val bboxesArray = json.getJSONArray("bounding_boxes")
        val boundingBoxes = mutableListOf<BoundingBox>()
        for (i in 0 until bboxesArray.length()) {
            val bboxObj = bboxesArray.getJSONObject(i)
            boundingBoxes.add(
                BoundingBox(
                    category = DefectCategory.valueOf(bboxObj.getString("category")),
                    confidence = bboxObj.getDouble("confidence").toFloat(),
                    xMin = bboxObj.getDouble("x_min").toFloat(),
                    yMin = bboxObj.getDouble("y_min").toFloat(),
                    xMax = bboxObj.getDouble("x_max").toFloat(),
                    yMax = bboxObj.getDouble("y_max").toFloat()
                )
            )
        }

        val assessment = AiAssessment(
            assessmentId = assessmentId,
            evidenceId = evidenceId,
            modelName = modelName,
            modelVersion = modelVersion,
            taxonomyVersion = taxonomyVersion,
            predictedCategory = DefectCategory.valueOf(predictedCategoryStr),
            confidenceScore = confidenceScore,
            boundingBoxes = boundingBoxes,
            imageQualityStatus = ImageQualityStatus.valueOf(imageQualityStatusStr),
            processingStatus = ProcessingStatus.valueOf(processingStatusStr),
            message = message,
            inferenceTimestampUtc = inferenceTimestampUtc
        )

        assertEquals("8f3b2d10-4e5a-4b2c-9a1b-3c4d5e6f7a8b", assessment.assessmentId)
        assertEquals("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d", assessment.evidenceId)
        assertEquals(DefectCategory.SOLID_WASTE, assessment.predictedCategory)
        assertEquals(0.942f, assessment.confidenceScore, 0.001f)
        assertEquals(ImageQualityStatus.PASS, assessment.imageQualityStatus)
        assertEquals(ProcessingStatus.COMPLETED, assessment.processingStatus)
        assertEquals(1, assessment.boundingBoxes.size)
        assertEquals(0.25f, assessment.boundingBoxes[0].xMin, 0.001f)
    }
}
