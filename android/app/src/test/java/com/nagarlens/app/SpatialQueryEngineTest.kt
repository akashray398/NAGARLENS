package com.nagarlens.app

import com.nagarlens.app.core.location.SpatialQueryEngine
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpatialQueryEngineTest {

    @Test
    fun `isValidCoordinate checks WGS84 ranges correctly`() {
        assertTrue(SpatialQueryEngine.isValidCoordinate(12.9716, 77.5946))
        assertTrue(SpatialQueryEngine.isValidCoordinate(0.0, 0.0))
        assertFalse(SpatialQueryEngine.isValidCoordinate(91.0, 77.0))
        assertFalse(SpatialQueryEngine.isValidCoordinate(12.0, 181.0))
        assertFalse(SpatialQueryEngine.isValidCoordinate(null, 77.0))
    }

    @Test
    fun `calculateHaversineDistanceMeters calculates accurate geodesic distance`() {
        // Point 1: Vidhana Soudha, Bangalore (12.9796°N, 77.5906°E)
        // Point 2: MG Road Metro Station, Bangalore (12.9756°N, 77.6067°E)
        val distanceMeters = SpatialQueryEngine.calculateHaversineDistanceMeters(
            12.9796, 77.5906,
            12.9756, 77.6067
        )

        // Expected distance is ~1800m - 1900m
        assertTrue(distanceMeters > 1700.0 && distanceMeters < 2000.0)
    }

    @Test
    fun `calculateBearingDegrees computes compass heading correctly`() {
        // Point A to Point B due East
        val bearingEast = SpatialQueryEngine.calculateBearingDegrees(0.0, 0.0, 0.0, 1.0)
        assertEquals(90.0, bearingEast, 0.5)

        // Point A to Point B due North
        val bearingNorth = SpatialQueryEngine.calculateBearingDegrees(0.0, 0.0, 1.0, 0.0)
        assertEquals(0.0, bearingNorth, 0.5)
    }

    @Test
    fun `calculateBoundingBox generates min max latitude and longitude bounds`() {
        val bbox = SpatialQueryEngine.calculateBoundingBox(12.9716, 77.5946, 1000.0)

        assertTrue(bbox.minLat < 12.9716)
        assertTrue(bbox.maxLat > 12.9716)
        assertTrue(bbox.minLon < 77.5946)
        assertTrue(bbox.maxLon > 77.5946)
    }

    @Test
    fun `clusterAssets groups nearby assets within cluster radius`() {
        val asset1 = Asset(id = "A1", assetCode = "NAG-CB-001", assetCategory = AssetCategory.CATCH_BASIN, latitude = 12.97160, longitude = 77.59460)
        val asset2 = Asset(id = "A2", assetCode = "NAG-CB-002", assetCategory = AssetCategory.CATCH_BASIN, latitude = 12.97162, longitude = 77.59462) // ~3m from asset1
        val asset3 = Asset(id = "A3", assetCode = "NAG-SWD-001", assetCategory = AssetCategory.STORM_WATER_DRAIN, latitude = 12.99000, longitude = 77.62000) // ~3km away

        val clusters = SpatialQueryEngine.clusterAssets(listOf(asset1, asset2, asset3), clusterRadiusMeters = 50.0)

        assertEquals(2, clusters.size)
        val cluster1 = clusters.find { it.memberCount == 2 }
        assertTrue(cluster1 != null)
        assertTrue(cluster1!!.memberAssetIds.contains("A1"))
        assertTrue(cluster1.memberAssetIds.contains("A2"))
    }
}
