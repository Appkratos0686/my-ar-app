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
        val normalizedPixelArray = FloatArray(targetWidth * targetHeight * 3)
        
        var pixelArrayIndex = 0
        for (yPixel in 0 until targetHeight) {
            for (xPixel in 0 until targetWidth) {
                val pixelColor = scaledBitmap.getPixel(xPixel, yPixel)
                
                // Extract RGB values and normalize to [0, 1]
                normalizedPixelArray[pixelArrayIndex++] = ((pixelColor shr 16) and 0xFF) / 255.0f
                normalizedPixelArray[pixelArrayIndex++] = ((pixelColor shr 8) and 0xFF) / 255.0f
                normalizedPixelArray[pixelArrayIndex++] = (pixelColor and 0xFF) / 255.0f
            }
        }
        
        return normalizedPixelArray
    }
    
    /**
     * Convert bitmap to ByteBuffer for TensorFlow Lite input
     */
    fun bitmapToByteBuffer(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): ByteBuffer {
        val tensorInputByteBuffer = ByteBuffer.allocateDirect(4 * targetWidth * targetHeight * 3)
        tensorInputByteBuffer.order(ByteOrder.nativeOrder())
        
        val normalizedPixelArray = normalize(bitmap, targetWidth, targetHeight)
        for (normalizedPixelValue in normalizedPixelArray) {
            tensorInputByteBuffer.putFloat(normalizedPixelValue)
        }
        
        return tensorInputByteBuffer
    }
    
    /**
     * Apply data augmentation for training (rotation, flip, etc.)
     */
    fun augment(bitmap: Bitmap, rotation: Float = 0f, flipHorizontal: Boolean = false): Bitmap {
        var augmentedBitmap = bitmap
        
        if (flipHorizontal) {
            val flipMatrix = android.graphics.Matrix()
            flipMatrix.preScale(-1.0f, 1.0f)
            augmentedBitmap = Bitmap.createBitmap(
                augmentedBitmap, 0, 0, augmentedBitmap.width, augmentedBitmap.height, flipMatrix, true
            )
        }
        
        if (rotation != 0f) {
            val rotationMatrix = android.graphics.Matrix()
            rotationMatrix.postRotate(rotation)
            augmentedBitmap = Bitmap.createBitmap(
                augmentedBitmap, 0, 0, augmentedBitmap.width, augmentedBitmap.height, rotationMatrix, true
            )
        }
        
        return augmentedBitmap
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
