package com.arroomscanner.app.models

/**
 * Represents different material types that can be detected
 */
enum class MaterialType {
    WOOD,
    CONCRETE,
    DRYWALL,
    BRICK,
    TILE,
    GLASS,
    METAL,
    PLASTIC,
    UNKNOWN
}

/**
 * Represents the result of material detection
 */
data class MaterialDetectionResult(
    val materialType: MaterialType,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)
