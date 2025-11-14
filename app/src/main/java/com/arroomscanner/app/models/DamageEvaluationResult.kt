package com.arroomscanner.app.models

/**
 * Represents different types of damage
 */
enum class DamageType {
    CRACK,
    WATER_DAMAGE,
    MOLD,
    STRUCTURAL,
    SURFACE_WEAR,
    NONE,
    UNKNOWN
}

/**
 * Represents the result of damage evaluation
 */
data class DamageEvaluationResult(
    val damageType: DamageType,
    val severity: Float, // 0.0 to 1.0
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)
