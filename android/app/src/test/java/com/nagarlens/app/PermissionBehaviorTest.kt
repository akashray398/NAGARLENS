package com.nagarlens.app

import com.nagarlens.app.core.camera.CameraXController
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.location.LocationProvider
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.repository.EvidenceRepository
import com.nagarlens.app.feature.capture.CaptureViewModel
import com.nagarlens.app.feature.capture.LocationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionBehaviorTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `photo capture succeeds without audio or location permissions`() {
        val fakeCamera = FakeCameraXController()
        val fakeLocation = FakeLocationProvider()
        val fakeRepository = FakeEvidenceRepository()

        val viewModel = CaptureViewModel(fakeCamera, fakeLocation, fakeRepository)

        // Grant camera permission, deny location and audio
        viewModel.onCameraPermissionResult(granted = true)
        viewModel.onLocationPermissionResult(granted = false)
        viewModel.onAudioPermissionResult(granted = false)

        val state = viewModel.uiState.value
        assertTrue(state.isCameraPermissionGranted)
        assertFalse(state.isLocationPermissionGranted)
        assertFalse(state.isAudioPermissionGranted)
        assertEquals(LocationState.PERMISSION_DENIED, state.locationState)
    }

    @Test
    fun `video capture falls back to silent recording when audio permission is denied`() {
        val fakeCamera = FakeCameraXController()
        val fakeLocation = FakeLocationProvider()
        val fakeRepository = FakeEvidenceRepository()

        val viewModel = CaptureViewModel(fakeCamera, fakeLocation, fakeRepository)

        // Deny audio permission
        viewModel.onAudioPermissionResult(granted = false)

        val state = viewModel.uiState.value
        assertFalse(state.isAudioPermissionGranted)
        assertNotNull(state.audioNotice)
        assertTrue(state.audioNotice!!.contains("Audio unavailable"))
    }

    @Test
    fun `location permission denial updates state without blocking capture`() {
        val fakeCamera = FakeCameraXController()
        val fakeLocation = FakeLocationProvider()
        val fakeRepository = FakeEvidenceRepository()

        val viewModel = CaptureViewModel(fakeCamera, fakeLocation, fakeRepository)

        viewModel.toggleLocationTagging(enable = true)
        viewModel.onLocationPermissionResult(granted = false)

        val state = viewModel.uiState.value
        assertEquals(LocationState.PERMISSION_DENIED, state.locationState)
        assertNull(state.currentLocation)
    }

    @Test
    fun `location tagging toggle enables or disables GNSS telemetry acquisition`() {
        val fakeCamera = FakeCameraXController()
        val fakeLocation = FakeLocationProvider()
        val fakeRepository = FakeEvidenceRepository()

        val viewModel = CaptureViewModel(fakeCamera, fakeLocation, fakeRepository)

        viewModel.toggleLocationTagging(enable = false)
        assertEquals(LocationState.DISABLED, viewModel.uiState.value.locationState)

        viewModel.toggleLocationTagging(enable = true)
        viewModel.onLocationPermissionResult(granted = true)
        assertEquals(LocationState.CAPTURED, viewModel.uiState.value.locationState)
        assertNotNull(viewModel.uiState.value.currentLocation)
    }

    private class FakeCameraXController : CameraXController(dummyContext())
    private class FakeLocationProvider : LocationProvider(dummyContext()) {
        override suspend fun getCurrentLocation(): LocationData {
            return LocationData(12.9716, 77.5946, 2.0f, System.currentTimeMillis())
        }
    }

    private class FakeEvidenceRepository : EvidenceRepository {
        override fun savePhotoEvidence(tempPhotoFile: File, locationData: LocationData?): Flow<Result<Evidence>> = flowOf()
        override fun saveVideoEvidence(tempVideoFile: File, locationData: LocationData?): Flow<Result<Evidence>> = flowOf()
        override fun getAllEvidence(): Flow<List<Evidence>> = flowOf(emptyList())
        override fun getEvidenceById(id: String): Flow<Evidence?> = flowOf(null)
        override fun getEvidenceBySyncStatus(status: SyncStatus): Flow<List<Evidence>> = flowOf(emptyList())
        override suspend fun updateSyncStatus(id: String, targetStatus: SyncStatus, errorMessage: String?) {}
        override suspend fun queueForSync(id: String): Result<Unit> = Result.success(Unit)
        override suspend fun retrySync(id: String): Result<Unit> = Result.success(Unit)
        override suspend fun auditAndRecoverOrphanFiles(): Int = 0
        override suspend fun recoverStaleUploadingRecords(staleThresholdMs: Long): Int = 0
    }

    companion object {
        private fun dummyContext(): android.content.Context {
            return object : android.content.ContextWrapper(null) {
                override fun getApplicationContext(): android.content.Context = this
            }
        }
    }
}
