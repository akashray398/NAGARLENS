package com.nagarlens.app.core.location

import com.nagarlens.app.core.model.Asset
import com.nagarlens.app.core.model.AssetCategory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class SpatialBoundingBox(
    val minLat: Double,
    val minLon: Double,
    val maxLat: Double,
    val maxLon: Double
)

data class AssetCluster(
    val clusterId: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val memberCount: Int,
    val primaryCategory: AssetCategory,
    val memberAssetIds: List<String>
)

object SpatialQueryEngine {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun isValidCoordinate(latitude: Double?, longitude: Double?): Boolean {
        if (latitude == null || longitude == null) return false
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /**
     * Calculates exact geodesic distance between two WGS84 coordinates using the Haversine formula.
     */
    fun calculateHaversineDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        if (!isValidCoordinate(lat1, lon1) || !isValidCoordinate(lat2, lon2)) return 0.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(rLat1) * cos(rLat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    /**
     * Computes compass bearing in degrees (0.0 to 360.0°) from point 1 to point 2.
     */
    fun calculateBearingDegrees(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        if (!isValidCoordinate(lat1, lon1) || !isValidCoordinate(lat2, lon2)) return 0.0

        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val dLon = Math.toRadians(lon2 - lon1)

        val y = sin(dLon) * cos(rLat2)
        val x = cos(rLat1) * sin(rLat2) - sin(rLat1) * cos(rLat2) * cos(dLon)
        val initialBearing = Math.toDegrees(atan2(y, x))

        return (initialBearing + 360.0) % 360.0
    }

    /**
     * Computes min/max latitude and longitude bounding box for a given center coordinate and search radius in meters.
     */
    fun calculateBoundingBox(
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double
    ): SpatialBoundingBox {
        val latDelta = Math.toDegrees(radiusMeters / EARTH_RADIUS_METERS)
        val lonDelta = Math.toDegrees(radiusMeters / (EARTH_RADIUS_METERS * cos(Math.toRadians(centerLat))))

        return SpatialBoundingBox(
            minLat = (centerLat - latDelta).coerceAtLeast(-90.0),
            minLon = (centerLon - lonDelta).coerceAtLeast(-180.0),
            maxLat = (centerLat + latDelta).coerceAtMost(90.0),
            maxLon = (centerLon + lonDelta).coerceAtMost(180.0)
        )
    }

    /**
     * Filters asset list by geographic radius in meters around reference point.
     */
    fun filterAssetsNearby(
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double,
        assets: List<Asset>
    ): List<Pair<Asset, Double>> {
        return assets.mapNotNull { asset ->
            val dist = calculateHaversineDistanceMeters(centerLat, centerLon, asset.latitude, asset.longitude)
            if (dist <= radiusMeters) asset to dist else null
        }.sortedBy { it.second }
    }

    /**
     * Groups nearby assets into spatial clusters for high-density viewport rendering.
     */
    fun clusterAssets(
        assets: List<Asset>,
        clusterRadiusMeters: Double = 50.0
    ): List<AssetCluster> {
        if (assets.isEmpty()) return emptyList()

        val visited = mutableSetOf<String>()
        val clusters = mutableListOf<AssetCluster>()

        for (asset in assets) {
            if (visited.contains(asset.id)) continue

            val members = assets.filter { other ->
                !visited.contains(other.id) &&
                        calculateHaversineDistanceMeters(asset.latitude, asset.longitude, other.latitude, other.longitude) <= clusterRadiusMeters
            }

            members.forEach { visited.add(it.id) }

            val avgLat = members.map { it.latitude }.average()
            val avgLon = members.map { it.longitude }.average()

            clusters.add(
                AssetCluster(
                    clusterId = "CLUSTER-${asset.id.take(8)}",
                    centerLatitude = avgLat,
                    centerLongitude = avgLon,
                    memberCount = members.size,
                    primaryCategory = asset.assetCategory,
                    memberAssetIds = members.map { it.id }
                )
            )
        }

        return clusters
    }
}
