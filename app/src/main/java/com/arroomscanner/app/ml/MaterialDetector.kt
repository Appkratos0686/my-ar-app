package com.arroomscanner.app.ml

import android.content.Context
import android.graphics.Bitmap
import com.arroomscanner.app.models.MaterialDetectionResult
import com.arroomscanner.app.models.MaterialType
import com.arroomscanner.app.utils.ImagePreprocessor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Material detection using TensorFlow Lite
 * Includes fallback model support
 */
class MaterialDetector(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    private var fallbackInterpreter: Interpreter? = null
    private var isUsingFallback = false
    
    private val inputWidth = 224
    private val inputHeight = 224
    
    companion object {
        private const val PRIMARY_MODEL = "material_detector.tflite"
        private const val FALLBACK_MODEL = "material_detector_fallback.tflite"
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
     * Detect material type from image
     */
    fun detect(bitmap: Bitmap): MaterialDetectionResult {
        val currentInterpreter = interpreter ?: return MaterialDetectionResult(
            MaterialType.UNKNOWN,
            0f
        )
        
        try {
            // Preprocess image
            val inputBuffer = ImagePreprocessor.bitmapToByteBuffer(bitmap, inputWidth, inputHeight)
            
            // Run inference
            val outputArray = Array(1) { FloatArray(MaterialType.values().size - 1) } // -1 for UNKNOWN
            currentInterpreter.run(inputBuffer, outputArray)
            
            // Find highest confidence
            val outputs = outputArray[0]
            val maxIndex = outputs.indices.maxByOrNull { outputs[it] } ?: 0
            val confidence = outputs[maxIndex]
            
            val materialType = when (maxIndex) {
                0 -> MaterialType.WOOD
                1 -> MaterialType.CONCRETE
                2 -> MaterialType.DRYWALL
                3 -> MaterialType.BRICK
                4 -> MaterialType.TILE
                5 -> MaterialType.GLASS
                6 -> MaterialType.METAL
                7 -> MaterialType.PLASTIC
                else -> MaterialType.UNKNOWN
            }
            
            return MaterialDetectionResult(materialType, confidence)
            
        } catch (e: Exception) {
            // Try fallback on error
            if (!isUsingFallback && fallbackInterpreter != null) {
                interpreter = fallbackInterpreter
                isUsingFallback = true
                return detect(bitmap)
            }
            
            return MaterialDetectionResult(MaterialType.UNKNOWN, 0f)
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
