package com.nagarlens.app.core.repository

import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.toDomainModel
import com.nagarlens.app.core.location.AssetCluster
import com.nagarlens.app.core.location.SpatialQueryEngine
import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.BoundaryType
import com.nagarlens.app.core.model.DigitalTwinAsset
import com.nagarlens.app.core.model.SpatialBoundary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpatialRepositoryImpl @Inject constructor(
    private val assetDao: AssetDao
) : SpatialRepository {

    override fun observeNearbyAssets(lat: Double, lon: Double, radiusMeters: Double): Flow<List<Pair<Asset, Double>>> {
        return assetDao.observeAllAssets().map { list ->
            val domainList = list.map { it.toDomainModel() }
            SpatialQueryEngine.filterAssetsNearby(lat, lon, radiusMeters, domainList)
        }
    }

    override fun observeViewportAssets(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): Flow<List<Asset>> {
        return assetDao.observeAssetsInViewport(minLat, minLon, maxLat, maxLon).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun getDigitalTwinAsset(assetId: String, userLat: Double?, userLon: Double?): DigitalTwinAsset? {
        val entity = assetDao.getAssetById(assetId) ?: return null
        val asset = entity.toDomainModel()

        val distance = if (userLat != null && userLon != null) {
            SpatialQueryEngine.calculateHaversineDistanceMeters(userLat, userLon, asset.latitude, asset.longitude)
        } else null

        val bearing = if (userLat != null && userLon != null) {
            SpatialQueryEngine.calculateBearingDegrees(userLat, userLon, asset.latitude, asset.longitude)
        } else null

        return DigitalTwinAsset(
            asset = asset,
            distanceToUserMeters = distance,
            bearingFromUserDegrees = bearing,
            linkedEvidenceCount = 2,
            openIncidentsCount = 1,
            locationCorrectionCount = 0,
            catchmentBoundaryName = "Ward 12 — Bangalore Central Catchment",
            lastInspectionTimestampUtc = System.currentTimeMillis() - (1000 * 3600 * 12)
        )
    }

    override fun observeSpatialBoundaries(): Flow<List<SpatialBoundary>> {
        return flowOf(
            listOf(
                SpatialBoundary(
                    id = "WARD-12-BOUNDARY",
                    name = "Ward 12 — Bangalore Central",
                    boundaryType = BoundaryType.MUNICIPAL_WARD,
                    wardNumber = 12,
                    minLatitude = 12.9600,
                    minLongitude = 77.5800,
                    maxLatitude = 12.9850,
                    maxLongitude = 77.6100,
                    totalRegisteredAssets = 24
                )
            )
        )
    }

    override suspend fun getSpatialClusters(radiusMeters: Double): List<AssetCluster> {
        val entities = assetDao.observeAllAssets().first()
        val domainList = entities.map { it.toDomainModel() }
        return SpatialQueryEngine.clusterAssets(domainList, clusterRadiusMeters = radiusMeters)
    }
}
