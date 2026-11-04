package com.nagarlens.app

import com.nagarlens.app.core.camera.CameraXController
import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.location.LocationProvider
import com.nagarlens.app.core.model.Evidence
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.repository.EvidenceRepository
import com.nagarlens.app.feature.activity.ActivityViewModel
import com.nagarlens.app.feature.capture.CameraMode
import com.nagarlens.app.feature.capture.CaptureViewModel
import com.nagarlens.app.feature.map.MapViewModel
import com.nagarlens.app.feature.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class FeatureViewModelsTest {

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
    fun `mapViewModel initializes state with active catchment bounds`() {
        val viewModel = MapViewModel()
        val state = viewModel.uiState.value
        assertNotNull(state.activeWard)
        assertEquals(4, state.activeFilters.size)

        viewModel.selectFilter("Catch Basins")
        assertEquals("Catch Basins", viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `captureViewModel initializes state with default camera mode`() {
        val fakeCamera = FakeCameraXController()
        val fakeLocation = FakeLocationProvider()
        val fakeRepository = FakeEvidenceRepository()

        val viewModel = CaptureViewModel(fakeCamera, fakeLocation, fakeRepository)
        val state = viewModel.uiState.value
        assertEquals(CameraMode.PHOTO, state.cameraMode)
    }

    @Test
    fun `activityViewModel initializes state observing evidence records`() {
        val fakeRepository = FakeEvidenceRepository()
        val viewModel = ActivityViewModel(fakeRepository)
        val state = viewModel.uiState.value
        assertNotNull(state.evidenceList)
    }

    @Test
    fun `profileViewModel initializes state with auditor identity`() {
        val viewModel = ProfileViewModel()
        val state = viewModel.uiState.value
        assertNotNull(state.inspectorName)
        assertNotNull(state.assignedWard)
    }

    private class FakeCameraXController : CameraXController(dummyContext())
    private class FakeLocationProvider : LocationProvider(dummyContext()) {
        override suspend fun getCurrentLocation(): LocationData? = null
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
