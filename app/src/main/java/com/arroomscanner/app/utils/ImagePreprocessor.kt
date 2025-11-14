package com.arroomscanner.app.utils

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Image preprocessing utilities for TensorFlow Lite models
 */
object ImagePreprocessor {
    
    /**
     * Normalize image pixel values to range [0, 1]
     */
    fun normalize(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): FloatArray {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        val floatArray = FloatArray(targetWidth * targetHeight * 3)
        
        var index = 0
        for (y in 0 until targetHeight) {
            for (x in 0 until targetWidth) {
                val pixel = scaledBitmap.getPixel(x, y)
                
                // Extract RGB values and normalize to [0, 1]
                floatArray[index++] = ((pixel shr 16) and 0xFF) / 255.0f
                floatArray[index++] = ((pixel shr 8) and 0xFF) / 255.0f
                floatArray[index++] = (pixel and 0xFF) / 255.0f
            }
        }
        
        return floatArray
    }
    
    /**
     * Convert bitmap to ByteBuffer for TensorFlow Lite input
     */
    fun bitmapToByteBuffer(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * targetWidth * targetHeight * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        
        val floatArray = normalize(bitmap, targetWidth, targetHeight)
        for (value in floatArray) {
            byteBuffer.putFloat(value)
        }
        
        return byteBuffer
    }
    
    /**
     * Apply data augmentation for training (rotation, flip, etc.)
     */
    fun augment(bitmap: Bitmap, rotation: Float = 0f, flipHorizontal: Boolean = false): Bitmap {
        var augmented = bitmap
        
        if (flipHorizontal) {
            val matrix = android.graphics.Matrix()
            matrix.preScale(-1.0f, 1.0f)
            augmented = Bitmap.createBitmap(
                augmented, 0, 0, augmented.width, augmented.height, matrix, true
            )
        }
        
        if (rotation != 0f) {
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotation)
            augmented = Bitmap.createBitmap(
                augmented, 0, 0, augmented.width, augmented.height, matrix, true
            )
        }
        
        return augmented
    }
    
    /**
     * Apply Gaussian blur for noise reduction
     */
    fun applyGaussianBlur(bitmap: Bitmap, radius: Float = 5f): Bitmap {
        // Simple implementation using RenderScript would be ideal
        // For now, return original bitmap
        return bitmap
    }
}
