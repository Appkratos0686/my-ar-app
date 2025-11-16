package com.arroomscanner.app.models

/**
 * Represents 3D spatial data from AR scanning
 */
data class SpatialData(
    val points: List<Point3D>,
    val planes: List<Plane3D>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a 3D point in space
 */
data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Represents a 3D plane detected by AR
 */
data class Plane3D(
    val centerPoint: Point3D,
    val normal: Point3D,
    val extentX: Float,
    val extentZ: Float,
    val type: PlaneType
)

/**
 * Types of planes that can be detected
 */
enum class PlaneType {
    HORIZONTAL_UPWARD_FACING,
    HORIZONTAL_DOWNWARD_FACING,
    VERTICAL,
    UNKNOWN
}
