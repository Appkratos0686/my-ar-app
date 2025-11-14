package com.arroomscanner.app

import com.arroomscanner.app.models.Plane3D
import com.arroomscanner.app.models.PlaneType
import com.arroomscanner.app.models.Point3D
import com.arroomscanner.app.models.SpatialData
import com.arroomscanner.app.utils.SpatialDataPreprocessor
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SpatialDataPreprocessor
 */
class SpatialDataPreprocessorTest {
    
    @Test
    fun testFilterOutliers_removesDistantPoints() {
        val points = listOf(
            Point3D(0f, 0f, 0f),
            Point3D(1f, 1f, 1f),
            Point3D(2f, 2f, 2f),
            Point3D(100f, 100f, 100f) // Outlier
        )
        
        val filtered = SpatialDataPreprocessor.filterOutliers(points, threshold = 2.0f)
        
        // Outlier should be removed
        assertTrue(filtered.size < points.size)
        assertFalse(filtered.any { it.x == 100f })
    }
    
    @Test
    fun testFilterOutliers_emptyList() {
        val points = emptyList<Point3D>()
        
        val filtered = SpatialDataPreprocessor.filterOutliers(points)
        
        assertTrue(filtered.isEmpty())
    }
    
    @Test
    fun testFilterOutliers_singlePoint() {
        val points = listOf(Point3D(1f, 1f, 1f))
        
        val filtered = SpatialDataPreprocessor.filterOutliers(points)
        
        assertEquals(1, filtered.size)
    }
    
    @Test
    fun testDownsample_reducesPointCount() {
        val points = (0 until 1000).map { i ->
            Point3D(i.toFloat(), i.toFloat(), i.toFloat())
        }
        val targetCount = 100
        
        val downsampled = SpatialDataPreprocessor.downsample(points, targetCount)
        
        assertEquals(targetCount, downsampled.size)
    }
    
    @Test
    fun testDownsample_smallerThanTarget() {
        val points = listOf(
            Point3D(0f, 0f, 0f),
            Point3D(1f, 1f, 1f)
        )
        val targetCount = 100
        
        val downsampled = SpatialDataPreprocessor.downsample(points, targetCount)
        
        assertEquals(points.size, downsampled.size)
    }
    
    @Test
    fun testNormalize_scalesPointsToUnitRange() {
        val points = listOf(
            Point3D(0f, 0f, 0f),
            Point3D(10f, 10f, 10f),
            Point3D(5f, 5f, 5f)
        )
        val spatialData = SpatialData(points, emptyList())
        
        val normalized = SpatialDataPreprocessor.normalize(spatialData)
        
        // All normalized points should be in [0, 1] range
        for (point in normalized.points) {
            assertTrue("X should be in [0,1], but was ${point.x}", point.x in 0f..1f)
            assertTrue("Y should be in [0,1], but was ${point.y}", point.y in 0f..1f)
            assertTrue("Z should be in [0,1], but was ${point.z}", point.z in 0f..1f)
        }
    }
    
    @Test
    fun testNormalize_emptyData() {
        val spatialData = SpatialData(emptyList(), emptyList())
        
        val normalized = SpatialDataPreprocessor.normalize(spatialData)
        
        assertTrue(normalized.points.isEmpty())
    }
    
    @Test
    fun testCalculateCentroid_correctCalculation() {
        val points = listOf(
            Point3D(0f, 0f, 0f),
            Point3D(2f, 2f, 2f),
            Point3D(4f, 4f, 4f)
        )
        
        val centroid = SpatialDataPreprocessor.calculateCentroid(points)
        
        assertEquals(2f, centroid.x, 0.01f)
        assertEquals(2f, centroid.y, 0.01f)
        assertEquals(2f, centroid.z, 0.01f)
    }
    
    @Test
    fun testCalculateCentroid_emptyList() {
        val points = emptyList<Point3D>()
        
        val centroid = SpatialDataPreprocessor.calculateCentroid(points)
        
        assertEquals(0f, centroid.x, 0.01f)
        assertEquals(0f, centroid.y, 0.01f)
        assertEquals(0f, centroid.z, 0.01f)
    }
    
    @Test
    fun testCalculateCentroid_singlePoint() {
        val points = listOf(Point3D(5f, 10f, 15f))
        
        val centroid = SpatialDataPreprocessor.calculateCentroid(points)
        
        assertEquals(5f, centroid.x, 0.01f)
        assertEquals(10f, centroid.y, 0.01f)
        assertEquals(15f, centroid.z, 0.01f)
    }
}
