package com.nagarlens.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.AssetEntity
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.database.toDomainModel
import com.nagarlens.app.core.model.AssetCategory
import com.nagarlens.app.core.model.AssetVerificationStatus
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
class RoomAssetDaoTest {

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
    fun `insertAsset persists record and observes via Flow`() = runTest {
        val id = UUID.randomUUID().toString()
        val entity = AssetEntity(
            id = id,
            assetCode = "NAG-CB-000001",
            assetCategory = AssetCategory.CATCH_BASIN.name,
            latitude = 12.9716,
            longitude = 77.5946,
            description = "Catch basin at 4th Cross"
        )

        assetDao.insertAsset(entity)

        val retrieved = assetDao.getAssetById(id)
        assertNotNull(retrieved)
        assertEquals("NAG-CB-000001", retrieved!!.assetCode)
        assertEquals(AssetVerificationStatus.UNVERIFIED.name, retrieved.verificationStatus)

        val allRecords = assetDao.observeAllAssets().first()
        assertEquals(1, allRecords.size)
        assertEquals(id, allRecords[0].id)
    }

    @Test
    fun `observeAssetsInViewport filters records within bounding box`() = runTest {
        val insideId = UUID.randomUUID().toString()
        val outsideId = UUID.randomUUID().toString()

        val inside = AssetEntity(
            id = insideId,
            assetCode = "NAG-CB-000001",
            assetCategory = AssetCategory.CATCH_BASIN.name,
            latitude = 12.9716,
            longitude = 77.5946
        )

        val outside = AssetEntity(
            id = outsideId,
            assetCode = "NAG-CB-000002",
            assetCategory = AssetCategory.CATCH_BASIN.name,
            latitude = 25.0000,
            longitude = 80.0000
        )

        assetDao.insertAsset(inside)
        assetDao.insertAsset(outside)

        val viewportAssets = assetDao.observeAssetsInViewport(
            minLat = 12.0,
            minLon = 77.0,
            maxLat = 13.0,
            maxLon = 78.0
        ).first()

        assertEquals(1, viewportAssets.size)
        assertEquals(insideId, viewportAssets[0].id)
    }
}
