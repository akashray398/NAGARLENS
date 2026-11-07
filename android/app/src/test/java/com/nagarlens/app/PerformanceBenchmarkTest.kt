package com.nagarlens.app

import com.nagarlens.app.core.location.SpatialQueryEngine
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureNanoTime

class PerformanceBenchmarkTest {

    @Test
    fun `haversineGeodesicDistance calculation throughput benchmark`() {
        val iterations = 10_000
        val nanos = measureNanoTime {
            for (i in 0 until iterations) {
                SpatialQueryEngine.calculateHaversineDistanceMeters(
                    12.9716 + (i * 0.00001), 77.5946 + (i * 0.00001),
                    12.9750, 77.6000
                )
            }
        }
        val elapsedMs = nanos / 1_000_000.0
        val opsPerMs = iterations / elapsedMs
        // Throughput must exceed 100 calculations per millisecond
        assertTrue("Elapsed: ${elapsedMs}ms for 10,000 ops", elapsedMs < 50.0)
    }

    @Test
    fun `spatialAssetClustering performance benchmark`() {
        val assets = (1..500).map { i ->
            Asset(
                id = "AST-$i",
                assetCode = "NAG-CB-%06d".format(i),
                assetCategory = AssetCategory.CATCH_BASIN,
                latitude = 12.9716 + (i * 0.00005),
                longitude = 77.5946 + (i * 0.00005)
            )
        }

        val nanos = measureNanoTime {
            val clusters = SpatialQueryEngine.clusterAssets(assets, clusterRadiusMeters = 50.0)
            assertTrue(clusters.isNotEmpty())
        }
        val elapsedMs = nanos / 1_000_000.0
        // 500 asset clustering must execute under 100ms
        assertTrue("Clustering elapsed: ${elapsedMs}ms", elapsedMs < 100.0)
    }
}
