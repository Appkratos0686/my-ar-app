package com.arroomscanner.app.utils

import com.arroomscanner.app.models.Point3D
import com.arroomscanner.app.models.SpatialData
import kotlin.math.sqrt

/**
 * Spatial data preprocessing utilities
 */
object SpatialDataPreprocessor {
    
    /**
     * Filter outlier points using statistical methods
     */
    fun filterOutliers(points: List<Point3D>, threshold: Float = 2.0f): List<Point3D> {
        if (points.isEmpty()) return emptyList()
        
        // Calculate mean distances
        val meanX = points.map { it.x }.average().toFloat()
        val meanY = points.map { it.y }.average().toFloat()
        val meanZ = points.map { it.z }.average().toFloat()
        
        // Calculate standard deviation
        val distances = points.map { point ->
            val dx = point.x - meanX
            val dy = point.y - meanY
            val dz = point.z - meanZ
            sqrt(dx * dx + dy * dy + dz * dz)
        }
        
        val meanDistance = distances.average().toFloat()
        val variance = distances.map { (it - meanDistance) * (it - meanDistance) }.average()
        val stdDev = sqrt(variance).toFloat()
        
        // Filter points beyond threshold
        return points.filterIndexed { index, _ ->
            distances[index] <= meanDistance + threshold * stdDev
        }
    }
    
    /**
     * Downsample point cloud for performance
     */
    fun downsample(points: List<Point3D>, targetCount: Int): List<Point3D> {
        if (points.size <= targetCount) return points
        
        val step = points.size / targetCount
        return points.filterIndexed { index, _ -> index % step == 0 }.take(targetCount)
    }
    
    /**
     * Normalize spatial coordinates to a standard range
     */
    fun normalize(spatialData: SpatialData): SpatialData {
        val points = spatialData.points
        if (points.isEmpty()) return spatialData
        
        // Find bounding box
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        val minZ = points.minOf { it.z }
        val maxZ = points.maxOf { it.z }
        
        val rangeX = maxX - minX
        val rangeY = maxY - minY
        val rangeZ = maxZ - minZ
        val maxRange = maxOf(rangeX, rangeY, rangeZ)
        
        // Normalize to [0, 1] range
        val normalizedPoints = points.map { point ->
            Point3D(
                x = if (maxRange > 0) (point.x - minX) / maxRange else 0f,
                y = if (maxRange > 0) (point.y - minY) / maxRange else 0f,
                z = if (maxRange > 0) (point.z - minZ) / maxRange else 0f
            )
        }
        
        return spatialData.copy(points = normalizedPoints)
    }
    
    /**
     * Calculate centroid of point cloud
     */
    fun calculateCentroid(points: List<Point3D>): Point3D {
        if (points.isEmpty()) return Point3D(0f, 0f, 0f)
        
        val sumX = points.sumOf { it.x.toDouble() }.toFloat()
        val sumY = points.sumOf { it.y.toDouble() }.toFloat()
        val sumZ = points.sumOf { it.z.toDouble() }.toFloat()
        
        return Point3D(
            x = sumX / points.size,
            y = sumY / points.size,
            z = sumZ / points.size
        )
    }
}
