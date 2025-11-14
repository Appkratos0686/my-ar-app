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
            val primaryModel = loadModelFile(PRIMARY_MODEL)
            interpreter = Interpreter(primaryModel)
            
            // Load fallback model
            try {
                val fallbackModel = loadModelFile(FALLBACK_MODEL)
                fallbackInterpreter = Interpreter(fallbackModel)
            } catch (e: Exception) {
                // Fallback model is optional
            }
            
            true
        } catch (e: Exception) {
            // Try fallback if primary fails
            try {
                val fallbackModel = loadModelFile(FALLBACK_MODEL)
                fallbackInterpreter = Interpreter(fallbackModel)
                interpreter = fallbackInterpreter
                isUsingFallback = true
                true
            } catch (e2: Exception) {
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
            val inputBuffer = ImagePreprocessor.bitmapToByteBuffer(bitmap, inputWidth, inputHeight)
            
            // Run inference - output includes damage type probabilities and severity
            val outputArray = Array(1) { FloatArray((DamageType.values().size - 2) + 1) } // -2 for UNKNOWN and NONE, +1 for severity
            currentInterpreter.run(inputBuffer, outputArray)
            
            // Parse outputs
            val outputs = outputArray[0]
            val damageTypeCount = DamageType.values().size - 2
            val damageTypeProbabilities = outputs.sliceArray(0 until damageTypeCount)
            val severity = outputs[damageTypeCount].coerceIn(0f, 1f)
            
            // Find highest confidence damage type
            val maxIndex = damageTypeProbabilities.indices.maxByOrNull { damageTypeProbabilities[it] } ?: 0
            val confidence = damageTypeProbabilities[maxIndex]
            
            val damageType = when (maxIndex) {
                0 -> DamageType.CRACK
                1 -> DamageType.WATER_DAMAGE
                2 -> DamageType.MOLD
                3 -> DamageType.STRUCTURAL
                4 -> DamageType.SURFACE_WEAR
                else -> DamageType.UNKNOWN
            }
            
            // If confidence is very low, consider no damage
            val finalDamageType = if (confidence < 0.3f) DamageType.NONE else damageType
            
            return DamageEvaluationResult(finalDamageType, severity, confidence)
            
        } catch (e: Exception) {
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
        val fileDescriptor = context.assets.openFd("models/$filename")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    /**
     * Release resources
     */
    fun close() {
        interpreter?.close()
        fallbackInterpreter?.close()
    }
}
