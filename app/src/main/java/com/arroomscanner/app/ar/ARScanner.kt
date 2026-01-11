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
        val pointCloudBuffer = pointCloud.points
        pointCloudBuffer.rewind()
        
        val newPoints = mutableListOf<Point3D>()
        while (pointCloudBuffer.hasRemaining()) {
            val xCoordinate = pointCloudBuffer.float
            val yCoordinate = pointCloudBuffer.float
            val zCoordinate = pointCloudBuffer.float
            val confidenceScore = pointCloudBuffer.float
            
            // Only add points with sufficient confidence
            if (confidenceScore > 0.5f) {
                newPoints.add(Point3D(xCoordinate, yCoordinate, zCoordinate))
            }
        }
        
        // Add to buffer with size limit
        pointBuffer.addAll(newPoints)
        if (pointBuffer.size > 10000) {
            // Keep most recent points
            val pointsToRemoveCount = pointBuffer.size - 10000
            repeat(pointsToRemoveCount) { pointBuffer.removeAt(0) }
        }
    }
    
    /**
     * Extract detected planes
     */
    private fun extractPlanes(frame: Frame) {
        planeBuffer.clear()
        
        for (detectedPlane in frame.updatedTrackables.filterIsInstance<Plane>()) {
            if (detectedPlane.trackingState == TrackingState.TRACKING) {
                val planeCenterPose = detectedPlane.centerPose
                val planeCenterPoint = Point3D(
                    planeCenterPose.tx(),
                    planeCenterPose.ty(),
                    planeCenterPose.tz()
                )
                
                // Get plane normal
                val forwardAxis = planeCenterPose.zAxis
                val planeNormalVector = Point3D(forwardAxis[0], forwardAxis[1], forwardAxis[2])
                
                val detectedPlaneType = when (detectedPlane.type) {
                    Plane.Type.HORIZONTAL_UPWARD_FACING -> PlaneType.HORIZONTAL_UPWARD_FACING
                    Plane.Type.HORIZONTAL_DOWNWARD_FACING -> PlaneType.HORIZONTAL_DOWNWARD_FACING
                    Plane.Type.VERTICAL -> PlaneType.VERTICAL
                    else -> PlaneType.UNKNOWN
                }
                
                planeBuffer.add(
                    Plane3D(
                        centerPoint = planeCenterPoint,
                        normal = planeNormalVector,
                        extentX = detectedPlane.extentX,
                        extentZ = detectedPlane.extentZ,
                        type = detectedPlaneType
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
