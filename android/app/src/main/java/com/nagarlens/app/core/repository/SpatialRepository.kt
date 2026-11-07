package com.nagarlens.app.core.repository

import com.nagarlens.app.core.location.AssetCluster
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.DigitalTwinAsset
import com.nagarlens.app.core.model.SpatialBoundary
import kotlinx.coroutines.flow.Flow

interface SpatialRepository {
    fun observeNearbyAssets(lat: Double, lon: Double, radiusMeters: Double): Flow<List<Pair<Asset, Double>>>
    fun observeViewportAssets(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): Flow<List<Asset>>
    suspend fun getDigitalTwinAsset(assetId: String, userLat: Double?, userLon: Double?): DigitalTwinAsset?
    fun observeSpatialBoundaries(): Flow<List<SpatialBoundary>>
    suspend fun getSpatialClusters(radiusMeters: Double): List<AssetCluster>
}
