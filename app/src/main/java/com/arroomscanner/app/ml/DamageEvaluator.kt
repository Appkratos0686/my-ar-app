package com.arroomscanner.app.ml

import android.content.Context
import android.graphics.Bitmap
import com.arroomscanner.app.models.DamageEvaluationResult
import com.arroomscanner.app.models.DamageType
import com.arroomscanner.app.utils.ImagePreprocessor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Damage evaluation using TensorFlow Lite
 * Includes fallback model support
 */
class DamageEvaluator(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    private var fallbackInterpreter: Interpreter? = null
    private var isUsingFallback = false
    
    private val inputWidth = 224
    private val inputHeight = 224
    
    companion object {
        private const val PRIMARY_MODEL = "damage_evaluator.tflite"
        private const val FALLBACK_MODEL = "damage_evaluator_fallback.tflite"
    }
    
    /**
     * Initialize the model
     */
    fun initialize(): Boolean {
        return try {
            // Try to load primary model
            val primaryModelBuffer = loadModelFile(PRIMARY_MODEL)
            interpreter = Interpreter(primaryModelBuffer)
            
            // Load fallback model
            try {
                val fallbackModelBuffer = loadModelFile(FALLBACK_MODEL)
                fallbackInterpreter = Interpreter(fallbackModelBuffer)
            } catch (fallbackLoadException: Exception) {
                // Fallback model is optional
            }
            
            true
        } catch (primaryLoadException: Exception) {
            // Try fallback if primary fails
            try {
                val fallbackModelBuffer = loadModelFile(FALLBACK_MODEL)
                fallbackInterpreter = Interpreter(fallbackModelBuffer)
                interpreter = fallbackInterpreter
                isUsingFallback = true
                true
            } catch (fallbackLoadException: Exception) {
                false
            }
        }
    }
    
    /**
     * Evaluate damage from image
     */
    fun evaluate(bitmap: Bitmap): DamageEvaluationResult {
        val currentInterpreter = interpreter ?: return DamageEvaluationResult(
            DamageType.UNKNOWN,
            0f,
            0f
        )
        
        try {
            // Preprocess image
            val modelInputBuffer = ImagePreprocessor.bitmapToByteBuffer(bitmap, inputWidth, inputHeight)
            
            // Run inference - output includes damage type probabilities and severity
            val modelOutputArray = Array(1) { FloatArray((DamageType.values().size - 2) + 1) } // -2 for UNKNOWN and NONE, +1 for severity
            currentInterpreter.run(modelInputBuffer, modelOutputArray)
            
            // Parse outputs
            val modelOutputValues = modelOutputArray[0]
            val damageTypeCount = DamageType.values().size - 2
            val damageTypeProbabilities = modelOutputValues.sliceArray(0 until damageTypeCount)
            val damageSeverityScore = modelOutputValues[damageTypeCount].coerceIn(0f, 1f)
            
            // Find highest confidence damage type
            val highestConfidenceIndex = damageTypeProbabilities.indices.maxByOrNull { damageTypeProbabilities[it] } ?: 0
            val detectionConfidence = damageTypeProbabilities[highestConfidenceIndex]
            
            val detectedDamageType = when (highestConfidenceIndex) {
                0 -> DamageType.CRACK
                1 -> DamageType.WATER_DAMAGE
                2 -> DamageType.MOLD
                3 -> DamageType.STRUCTURAL
                4 -> DamageType.SURFACE_WEAR
                else -> DamageType.UNKNOWN
            }
            
            // If confidence is very low, consider no damage
            val finalDamageType = if (detectionConfidence < 0.3f) DamageType.NONE else detectedDamageType
            
            return DamageEvaluationResult(finalDamageType, damageSeverityScore, detectionConfidence)
            
        } catch (evaluationException: Exception) {
            // Try fallback on error
            if (!isUsingFallback && fallbackInterpreter != null) {
                interpreter = fallbackInterpreter
                isUsingFallback = true
                return evaluate(bitmap)
            }
            
            return DamageEvaluationResult(DamageType.UNKNOWN, 0f, 0f)
        }
    }
    
    /**
     * Check if using fallback model
     */
    fun isUsingFallbackModel(): Boolean = isUsingFallback
    
    /**
     * Load model file from assets
     */
    private fun loadModelFile(filename: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd("models/$filename")
        val modelFileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val modelFileChannel = modelFileInputStream.channel
        val modelStartOffset = assetFileDescriptor.startOffset
        val modelDeclaredLength = assetFileDescriptor.declaredLength
        return modelFileChannel.map(FileChannel.MapMode.READ_ONLY, modelStartOffset, modelDeclaredLength)
    }
    
    /**
     * Release resources
     */
    fun close() {
        interpreter?.close()
        fallbackInterpreter?.close()
    }
}
