package com.arroomscanner.app

import android.graphics.Bitmap
import com.arroomscanner.app.utils.ImagePreprocessor
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Unit tests for ImagePreprocessor
 */
class ImagePreprocessorTest {
    
    @Test
    fun testNormalize_returnsCorrectArraySize() {
        val bitmap = createMockBitmap(100, 100)
        val targetWidth = 50
        val targetHeight = 50
        
        val result = ImagePreprocessor.normalize(bitmap, targetWidth, targetHeight)
        
        assertEquals(targetWidth * targetHeight * 3, result.size)
    }
    
    @Test
    fun testNormalize_valuesInCorrectRange() {
        val bitmap = createMockBitmap(10, 10)
        
        val result = ImagePreprocessor.normalize(bitmap, 10, 10)
        
        // All values should be between 0 and 1
        for (value in result) {
            assertTrue("Value $value should be >= 0", value >= 0f)
            assertTrue("Value $value should be <= 1", value <= 1f)
        }
    }
    
    @Test
    fun testBitmapToByteBuffer_hasCorrectCapacity() {
        val bitmap = createMockBitmap(224, 224)
        
        val byteBuffer = ImagePreprocessor.bitmapToByteBuffer(bitmap, 224, 224)
        
        // 4 bytes per float, 3 channels (RGB)
        val expectedCapacity = 4 * 224 * 224 * 3
        assertEquals(expectedCapacity, byteBuffer.capacity())
    }
    
    @Test
    fun testAugment_flipHorizontal() {
        val bitmap = createMockBitmap(100, 100)
        
        val augmented = ImagePreprocessor.augment(bitmap, flipHorizontal = true)
        
        assertNotNull(augmented)
        assertEquals(bitmap.width, augmented.width)
        assertEquals(bitmap.height, augmented.height)
    }
    
    @Test
    fun testAugment_rotation() {
        val bitmap = createMockBitmap(100, 100)
        
        val augmented = ImagePreprocessor.augment(bitmap, rotation = 90f)
        
        assertNotNull(augmented)
    }
    
    @Test
    fun testAugment_combinedTransformations() {
        val bitmap = createMockBitmap(100, 100)
        
        val augmented = ImagePreprocessor.augment(
            bitmap,
            rotation = 45f,
            flipHorizontal = true
        )
        
        assertNotNull(augmented)
    }
    
    @Test
    fun testApplyGaussianBlur_returnsValidBitmap() {
        val bitmap = createMockBitmap(100, 100)
        
        val blurred = ImagePreprocessor.applyGaussianBlur(bitmap)
        
        assertNotNull(blurred)
        assertEquals(bitmap.width, blurred.width)
        assertEquals(bitmap.height, blurred.height)
    }
    
    private fun createMockBitmap(width: Int, height: Int): Bitmap {
        val bitmap = mock(Bitmap::class.java)
        `when`(bitmap.width).thenReturn(width)
        `when`(bitmap.height).thenReturn(height)
        `when`(bitmap.getPixel(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
            .thenReturn(0xFF808080.toInt()) // Gray color
        return bitmap
    }
}
