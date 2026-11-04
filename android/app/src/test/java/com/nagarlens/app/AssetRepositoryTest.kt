package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.AssetVerificationStatus
import com.nagarlens.app.core.model.VerificationDecision
import com.nagarlens.app.core.repository.AssetRepositoryImpl
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AssetRepositoryTest {

    private lateinit var database: NagarlensDatabase
    private lateinit var assetDao: AssetDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NagarlensDatabase::class.java
        ).allowMainThreadQueries().build()
        assetDao = database.assetDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `registerAsset assigns operational asset code and persists in Room`() = runTest {
        val repository = AssetRepositoryImpl(assetDao)

        val result = repository.registerAsset(
            category = AssetCategory.CATCH_BASIN,
            latitude = 12.9716,
            longitude = 77.5946,
            description = "Test Drain Catch Basin"
        )

        assertTrue(result.isSuccess)
        val asset = result.getOrThrow()

        assertEquals("NAG-CB-000001", asset.assetCode)
        assertEquals(AssetCategory.CATCH_BASIN, asset.assetCategory)
        assertEquals(AssetVerificationStatus.UNVERIFIED, asset.verificationStatus)

        val allAssets = repository.getAllAssets().first()
        assertEquals(1, allAssets.size)
        assertEquals(asset.id, allAssets[0].id)
    }

    @Test
    fun `submitVerification updates verification status to VERIFIED`() = runTest {
        val repository = AssetRepositoryImpl(assetDao)

        val regResult = repository.registerAsset(
            category = AssetCategory.STORM_WATER_DRAIN,
            latitude = 12.9716,
            longitude = 77.5946
        ).getOrThrow()

        val verResult = repository.submitVerification(
            assetId = regResult.id,
            decision = VerificationDecision.APPROVED,
            reviewerId = "reviewer_01"
        )

        assertTrue(verResult.isSuccess)
        val verifiedAsset = verResult.getOrThrow()

        assertEquals(AssetVerificationStatus.VERIFIED, verifiedAsset.verificationStatus)
        assertEquals(2, verifiedAsset.version)
    }
}
