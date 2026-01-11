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
        
        // Calculate mean coordinates
        val meanXCoordinate = points.map { it.x }.average().toFloat()
        val meanYCoordinate = points.map { it.y }.average().toFloat()
        val meanZCoordinate = points.map { it.z }.average().toFloat()
        
        // Calculate distance from mean for each point
        val distancesFromMean = points.map { point ->
            val xDelta = point.x - meanXCoordinate
            val yDelta = point.y - meanYCoordinate
            val zDelta = point.z - meanZCoordinate
            sqrt(xDelta * xDelta + yDelta * yDelta + zDelta * zDelta)
        }
        
        val meanDistance = distancesFromMean.average().toFloat()
        val variance = distancesFromMean.map { (it - meanDistance) * (it - meanDistance) }.average()
        val standardDeviation = sqrt(variance).toFloat()
        
        // Filter points beyond threshold
        return points.filterIndexed { index, _ ->
            distancesFromMean[index] <= meanDistance + threshold * standardDeviation
        }
    }
    
    /**
     * Downsample point cloud for performance
     */
    fun downsample(points: List<Point3D>, targetCount: Int): List<Point3D> {
        if (points.size <= targetCount) return points
        
        val samplingStepSize = points.size / targetCount
        return points.filterIndexed { index, _ -> index % samplingStepSize == 0 }.take(targetCount)
    }
    
    /**
     * Normalize spatial coordinates to a standard range
     */
    fun normalize(spatialData: SpatialData): SpatialData {
        val points = spatialData.points
        if (points.isEmpty()) return spatialData
        
        // Find bounding box
        val minimumXCoordinate = points.minOf { it.x }
        val maximumXCoordinate = points.maxOf { it.x }
        val minimumYCoordinate = points.minOf { it.y }
        val maximumYCoordinate = points.maxOf { it.y }
        val minimumZCoordinate = points.minOf { it.z }
        val maximumZCoordinate = points.maxOf { it.z }
        
        val xCoordinateRange = maximumXCoordinate - minimumXCoordinate
        val yCoordinateRange = maximumYCoordinate - minimumYCoordinate
        val zCoordinateRange = maximumZCoordinate - minimumZCoordinate
        val maximumCoordinateRange = maxOf(xCoordinateRange, yCoordinateRange, zCoordinateRange)
        
        // Normalize to [0, 1] range
        val normalizedPoints = points.map { point ->
            Point3D(
                x = if (maximumCoordinateRange > 0) (point.x - minimumXCoordinate) / maximumCoordinateRange else 0f,
                y = if (maximumCoordinateRange > 0) (point.y - minimumYCoordinate) / maximumCoordinateRange else 0f,
                z = if (maximumCoordinateRange > 0) (point.z - minimumZCoordinate) / maximumCoordinateRange else 0f
            )
        }
        
        return spatialData.copy(points = normalizedPoints)
    }
    
    /**
     * Calculate centroid of point cloud
     */
    fun calculateCentroid(points: List<Point3D>): Point3D {
        if (points.isEmpty()) return Point3D(0f, 0f, 0f)
        
        val totalXCoordinate = points.sumOf { it.x.toDouble() }.toFloat()
        val totalYCoordinate = points.sumOf { it.y.toDouble() }.toFloat()
        val totalZCoordinate = points.sumOf { it.z.toDouble() }.toFloat()
        
        return Point3D(
            x = totalXCoordinate / points.size,
            y = totalYCoordinate / points.size,
            z = totalZCoordinate / points.size
        )
    }
}
