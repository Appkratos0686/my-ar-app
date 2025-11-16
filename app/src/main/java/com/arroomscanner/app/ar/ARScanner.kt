package com.arroomscanner.app.ar

import com.arroomscanner.app.models.Plane3D
import com.arroomscanner.app.models.PlaneType
import com.arroomscanner.app.models.Point3D
import com.arroomscanner.app.models.SpatialData
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.PointCloud
import com.google.ar.core.TrackingState

/**
 * AR Scanner for processing spatial data from ARCore
 */
class ARScanner {
    
    private val pointBuffer = mutableListOf<Point3D>()
    private val planeBuffer = mutableListOf<Plane3D>()
    
    /**
     * Process AR frame and extract spatial data
     */
    fun processFrame(frame: Frame): SpatialData? {
        if (frame.camera.trackingState != TrackingState.TRACKING) {
            return null
        }
        
        // Extract point cloud
        val pointCloud = frame.acquirePointCloud()
        extractPoints(pointCloud)
        pointCloud.release()
        
        // Extract planes
        extractPlanes(frame)
        
        return SpatialData(
            points = pointBuffer.toList(),
            planes = planeBuffer.toList()
        )
    }
    
    /**
     * Extract 3D points from point cloud
     */
    private fun extractPoints(pointCloud: PointCloud) {
        val points = pointCloud.points
        points.rewind()
        
        val newPoints = mutableListOf<Point3D>()
        while (points.hasRemaining()) {
            val x = points.float
            val y = points.float
            val z = points.float
            val confidence = points.float
            
            // Only add points with sufficient confidence
            if (confidence > 0.5f) {
                newPoints.add(Point3D(x, y, z))
            }
        }
        
        // Add to buffer with size limit
        pointBuffer.addAll(newPoints)
        if (pointBuffer.size > 10000) {
            // Keep most recent points
            val removeCount = pointBuffer.size - 10000
            repeat(removeCount) { pointBuffer.removeAt(0) }
        }
    }
    
    /**
     * Extract detected planes
     */
    private fun extractPlanes(frame: Frame) {
        planeBuffer.clear()
        
        for (plane in frame.updatedTrackables.filterIsInstance<Plane>()) {
            if (plane.trackingState == TrackingState.TRACKING) {
                val centerPose = plane.centerPose
                val center = Point3D(
                    centerPose.tx(),
                    centerPose.ty(),
                    centerPose.tz()
                )
                
                // Get plane normal
                val forward = centerPose.zAxis
                val normal = Point3D(forward[0], forward[1], forward[2])
                
                val planeType = when (plane.type) {
                    Plane.Type.HORIZONTAL_UPWARD_FACING -> PlaneType.HORIZONTAL_UPWARD_FACING
                    Plane.Type.HORIZONTAL_DOWNWARD_FACING -> PlaneType.HORIZONTAL_DOWNWARD_FACING
                    Plane.Type.VERTICAL -> PlaneType.VERTICAL
                    else -> PlaneType.UNKNOWN
                }
                
                planeBuffer.add(
                    Plane3D(
                        centerPoint = center,
                        normal = normal,
                        extentX = plane.extentX,
                        extentZ = plane.extentZ,
                        type = planeType
                    )
                )
            }
        }
    }
    
    /**
     * Clear accumulated data
     */
    fun clear() {
        pointBuffer.clear()
        planeBuffer.clear()
    }
    
    /**
     * Get current point count
     */
    fun getPointCount(): Int = pointBuffer.size
    
    /**
     * Get current plane count
     */
    fun getPlaneCount(): Int = planeBuffer.size
}
