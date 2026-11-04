package com.nagarlens.app

import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.ml.UnavailableDefectAnalyzer
import com.nagarlens.app.core.model.DefectCategory
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.MediaType
import com.nagarlens.app.core.model.ProcessingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UnavailableDefectAnalyzerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val sha256Hasher = Sha256Hasher()
    private val analyzer = UnavailableDefectAnalyzer(sha256Hasher, Dispatchers.Unconfined)

    @Test
    fun `getModelInfo reports model as unavailable when weights are missing`() {
        val info = analyzer.getModelInfo()
        assertEquals("MobileNetV3-Small-DrainDefect", info.modelName)
        assertFalse(info.isAvailable)
    }

    @Test
    fun `analyzeEvidence returns explicit MODEL_UNAVAILABLE processing status`() = runTest {
        val mediaFile = tempFolder.newFile("test_drain_photo.jpg")
        mediaFile.writeBytes("SAMPLE_DRAIN_PHOTO_BINARY_DATA".toByteArray())
        val hash = sha256Hasher.calculateFileSha256(mediaFile)

        val evidence = Evidence(
            id = "EVID-AI-001",
            mediaUri = mediaFile.toURI().toString(),
            mediaType = MediaType.PHOTO,
            originalFileName = mediaFile.name,
            mimeType = "image/jpeg",
            fileSizeBytes = mediaFile.length(),
            captureTimestampUtc = System.currentTimeMillis(),
            sha256Hash = hash
        )

        val assessment = analyzer.analyzeEvidence(evidence, mediaFile)

        assertEquals("EVID-AI-001", assessment.evidenceId)
        assertEquals(ProcessingStatus.MODEL_UNAVAILABLE, assessment.processingStatus)
        assertEquals(DefectCategory.UNKNOWN_INSUFFICIENT, assessment.predictedCategory)
        assertEquals(0.0f, assessment.confidenceScore, 0.001f)
        assertTrue(assessment.message.contains("ready for model artifact deployment"))
    }

    @Test
    fun `analyzeEvidence fails when media file is missing or tampered`() = runTest {
        val mediaFile = tempFolder.newFile("tampered_drain_photo.jpg")
        mediaFile.writeBytes("CORRUPT_BYTES".toByteArray())

        val evidence = Evidence(
            id = "EVID-AI-002",
            mediaUri = mediaFile.toURI().toString(),
            mediaType = MediaType.PHOTO,
            originalFileName = mediaFile.name,
            mimeType = "image/jpeg",
            fileSizeBytes = mediaFile.length(),
            captureTimestampUtc = System.currentTimeMillis(),
            sha256Hash = "0000000000000000000000000000000000000000000000000000000000000000"
        )

        val assessment = analyzer.analyzeEvidence(evidence, mediaFile)

        assertEquals(ProcessingStatus.FAILED, assessment.processingStatus)
        assertTrue(assessment.message.contains("integrity check failed"))
    }
}
