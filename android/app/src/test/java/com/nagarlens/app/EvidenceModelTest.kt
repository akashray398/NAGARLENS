package com.nagarlens.app

import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.EvidenceStatus
import com.nagarlens.app.core.model.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class EvidenceModelTest {

    @Test
    fun `evidence model retains location metadata when available`() {
        val id = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val locationData = LocationData(
            latitude = 12.9716,
            longitude = 77.5946,
            accuracyMeters = 2.1f,
            timestampUtc = timestamp
        )

        val evidence = Evidence(
            id = id,
            mediaUri = "file:///data/user/0/com.nagarlens.app/files/evidence/EVID_$id.jpg",
            mediaType = MediaType.PHOTO,
            originalFileName = "EVID_$id.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = 1024L,
            captureTimestampUtc = timestamp,
            latitude = locationData.latitude,
            longitude = locationData.longitude,
            locationAccuracyMeters = locationData.accuracyMeters,
            locationTimestampUtc = locationData.timestampUtc,
            sha256Hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            status = EvidenceStatus.RECORDED
        )

        assertEquals(id, evidence.id)
        assertEquals(12.9716, evidence.latitude!!, 0.0001)
        assertEquals(77.5946, evidence.longitude!!, 0.0001)
        assertEquals(2.1f, evidence.locationAccuracyMeters!!, 0.01f)
        assertEquals(EvidenceStatus.RECORDED, evidence.status)
    }

    @Test
    fun `evidence capture succeeds even when location is denied or unavailable`() {
        val id = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val evidenceWithoutLocation = Evidence(
            id = id,
            mediaUri = "file:///data/user/0/com.nagarlens.app/files/evidence/EVID_$id.jpg",
            mediaType = MediaType.PHOTO,
            originalFileName = "EVID_$id.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = 2048L,
            captureTimestampUtc = timestamp,
            latitude = null,
            longitude = null,
            locationAccuracyMeters = null,
            locationTimestampUtc = null,
            sha256Hash = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3",
            status = EvidenceStatus.RECORDED
        )

        assertNull(evidenceWithoutLocation.latitude)
        assertNull(evidenceWithoutLocation.longitude)
        assertNotNull(evidenceWithoutLocation.sha256Hash)
        assertEquals(EvidenceStatus.RECORDED, evidenceWithoutLocation.status)
    }
}
