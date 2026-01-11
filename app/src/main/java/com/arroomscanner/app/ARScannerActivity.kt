package com.arroomscanner.app

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arroomscanner.app.ar.ARScanner
import com.arroomscanner.app.databinding.ActivityArScannerBinding
import com.arroomscanner.app.ml.DamageEvaluator
import com.arroomscanner.app.ml.MaterialDetector
import com.arroomscanner.app.models.DamageType
import com.arroomscanner.app.models.MaterialType
import com.arroomscanner.app.utils.SpatialDataPreprocessor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ARScannerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityArScannerBinding
    private var arSession: Session? = null
    private val arScanner = ARScanner()
    private lateinit var materialDetector: MaterialDetector
    private lateinit var damageEvaluator: DamageEvaluator
    
    private var isScanning = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeML()
        setupAR()
        setupUI()
    }
    
    private fun initializeML() {
        lifecycleScope.launch(Dispatchers.IO) {
            materialDetector = MaterialDetector(applicationContext)
            damageEvaluator = DamageEvaluator(applicationContext)
            
            val isMaterialDetectorInitialized = materialDetector.initialize()
            val isDamageEvaluatorInitialized = damageEvaluator.initialize()
            
            withContext(Dispatchers.Main) {
                if (!isMaterialDetectorInitialized || !isDamageEvaluatorInitialized) {
                    updateStatus("Warning: Some AI models failed to load. Using fallback models.")
                }
                
                if (materialDetector.isUsingFallbackModel() || damageEvaluator.isUsingFallbackModel()) {
                    updateStatus(getString(R.string.fallback_model_active))
                }
            }
        }
    }
    
    private fun setupAR() {
        // Check AR availability
        val arCoreAvailability = ArCoreApk.getInstance().checkAvailability(this)
        if (arCoreAvailability.isTransient) {
            // Re-query at 5Hz while compatibility is checked in the background.
            return
        }
        
        if (!arCoreAvailability.isSupported) {
            Toast.makeText(this, R.string.ar_not_supported, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        try {
            arSession = Session(this)
            val sessionConfig = Config(arSession)
            sessionConfig.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            sessionConfig.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            arSession?.configure(sessionConfig)
        } catch (sessionCreationException: Exception) {
            Toast.makeText(this, "Failed to create AR session: ${sessionCreationException.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun setupUI() {
        binding.scanButton.setOnClickListener {
            toggleScanning()
        }
    }
    
    private fun toggleScanning() {
        isScanning = !isScanning
        
        if (isScanning) {
            binding.scanButton.text = getString(R.string.stop_scanning)
            startScanning()
        } else {
            binding.scanButton.text = getString(R.string.start_scanning)
            stopScanning()
        }
    }
    
    private fun startScanning() {
        updateStatus(getString(R.string.processing_spatial_data))
        
        lifecycleScope.launch {
            try {
                arSession?.resume()
                
                // Simulate scanning process
                while (isScanning) {
                    processARFrame()
                    kotlinx.coroutines.delay(100) // Process at ~10 FPS
                }
            } catch (scanningException: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ARScannerActivity,
                        "Error during scanning: ${scanningException.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun stopScanning() {
        arSession?.pause()
    }
    
    private suspend fun processARFrame() {
        arSession?.let { session ->
            try {
                val arFrame = session.update()
                
                // Process spatial data
                val scannedSpatialData = arScanner.processFrame(arFrame)
                
                scannedSpatialData?.let { rawSpatialData ->
                    // Preprocess spatial data
                    val preprocessedSpatialData = withContext(Dispatchers.Default) {
                        val filteredPoints = SpatialDataPreprocessor.filterOutliers(rawSpatialData.points)
                        val downsampledPoints = SpatialDataPreprocessor.downsample(filteredPoints, 1000)
                        rawSpatialData.copy(points = downsampledPoints)
                    }
                    
                    withContext(Dispatchers.Main) {
                        updateStatus("Points: ${preprocessedSpatialData.points.size}, Planes: ${preprocessedSpatialData.planes.size}")
                    }
                    
                    // Periodically run ML detection (every 1 second)
                    if (System.currentTimeMillis() % 1000 < 100) {
                        runMLDetection()
                    }
                }
            } catch (frameProcessingException: Exception) {
                // Silently handle frame processing errors
            }
        }
    }
    
    private suspend fun runMLDetection() {
        // In a real app, capture the camera frame as a bitmap
        // For now, we'll show the detection flow without actual detection
        withContext(Dispatchers.Main) {
            updateStatus(getString(R.string.detecting_materials))
        }
        
        // Simulate detection results
        withContext(Dispatchers.Main) {
            updateMaterialText("Material: Concrete (Confidence: 85.3%)")
            updateDamageText("Damage: Minor Surface Wear (Severity: 25.0%)")
        }
    }
    
    private fun updateStatus(status: String) {
        binding.statusText.text = status
    }
    
    private fun updateMaterialText(text: String) {
        binding.materialText.text = text
    }
    
    private fun updateDamageText(text: String) {
        binding.damageText.text = text
    }
    
    override fun onPause() {
        super.onPause()
        arSession?.pause()
    }
    
    override fun onResume() {
        super.onResume()
        try {
            arSession?.resume()
        } catch (resumeException: Exception) {
            // Handle resume error
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        arSession?.close()
        materialDetector.close()
        damageEvaluator.close()
    }
}
