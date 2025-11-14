# AR Room Scanner

An innovative mobile app that leverages augmented reality (AR) and TensorFlow Lite to scan and analyze rooms in 3D. It detects material types, evaluates damage, and processes spatial data in real-time.

## Features

### 🎯 Core Capabilities
- **3D Room Scanning**: Real-time AR-based room scanning using ARCore
- **Material Detection**: AI-powered identification of building materials (wood, concrete, drywall, brick, tile, glass, metal, plastic)
- **Damage Evaluation**: Automated detection and assessment of structural damage (cracks, water damage, mold, structural issues, surface wear)
- **Spatial Data Processing**: Advanced 3D point cloud and plane detection
- **Fallback AI Models**: Robust model architecture with automatic fallback for reliability
- **Real-time Analysis**: Instantaneous processing and feedback during scanning

### 🔧 Technical Features
- **TensorFlow Lite Integration**: On-device ML inference for fast, private processing
- **Intuitive Label Assets**: Pre-configured label files for material and damage types
- **Robust Preprocessing**: Comprehensive image and spatial data preprocessing utilities
- **Outlier Filtering**: Statistical methods to clean spatial data
- **Point Cloud Downsampling**: Performance optimization for large datasets
- **Data Normalization**: Standardized input processing for ML models

## Use Cases

- **Home Improvement**: Scan rooms before renovation to identify materials and damage
- **Construction**: Quick assessment of building conditions and materials
- **Design Professionals**: Accurate spatial measurements and material documentation
- **Property Inspection**: Comprehensive damage evaluation for insurance and maintenance
- **Real Estate**: Detailed property condition reports

## Architecture

### Components

#### AR Module (`app/src/main/java/com/arroomscanner/app/ar/`)
- `ARScanner.kt`: Core AR scanning engine using ARCore
  - Point cloud extraction with confidence filtering
  - Plane detection (horizontal and vertical)
  - Real-time spatial data processing

#### ML Module (`app/src/main/java/com/arroomscanner/app/ml/`)
- `MaterialDetector.kt`: TensorFlow Lite-based material classification
  - 8 material types supported
  - Primary and fallback model support
  - Automatic model switching on errors
  
- `DamageEvaluator.kt`: TensorFlow Lite-based damage assessment
  - 5 damage types + severity scoring
  - Confidence-based damage classification
  - Fallback model support

#### Data Models (`app/src/main/java/com/arroomscanner/app/models/`)
- `MaterialDetectionResult.kt`: Material detection output
- `DamageEvaluationResult.kt`: Damage evaluation output
- `SpatialData.kt`: 3D point cloud and plane data structures

#### Utilities (`app/src/main/java/com/arroomscanner/app/utils/`)
- `ImagePreprocessor.kt`: Image preprocessing for ML models
  - Normalization to [0, 1] range
  - Bitmap to ByteBuffer conversion
  - Data augmentation (rotation, flip)
  - Gaussian blur for noise reduction
  
- `SpatialDataPreprocessor.kt`: Spatial data preprocessing
  - Outlier filtering using statistical methods
  - Point cloud downsampling
  - Coordinate normalization
  - Centroid calculation

#### UI (`app/src/main/java/com/arroomscanner/app/`)
- `MainActivity.kt`: Entry point with permission handling
- `ARScannerActivity.kt`: Main AR scanning interface

### ML Models

Models are located in `app/src/main/assets/models/`:
- `material_detector.tflite`: Primary material classification model
- `material_detector_fallback.tflite`: Lightweight fallback model
- `damage_evaluator.tflite`: Primary damage assessment model
- `damage_evaluator_fallback.tflite`: Lightweight fallback model

**Input Specifications:**
- Image size: 224x224 pixels
- Color space: RGB
- Normalization: [0, 1] range

**Output Specifications:**
- Material Detector: 8 class probabilities
- Damage Evaluator: 5 damage type probabilities + 1 severity score

### Label Assets

Label files in `app/src/main/assets/labels/`:
- `material_labels.txt`: Material type labels
- `damage_labels.txt`: Damage type labels

## Testing

Comprehensive unit tests for preprocessing utilities:
- `ImagePreprocessorTest.kt`: 8 test cases for image preprocessing
  - Normalization validation
  - Array size verification
  - Value range checking
  - Augmentation testing
  
- `SpatialDataPreprocessorTest.kt`: 10 test cases for spatial preprocessing
  - Outlier detection and removal
  - Downsampling accuracy
  - Normalization validation
  - Centroid calculation

Run tests:
```bash
./gradlew test
```

## Requirements

- **Android SDK**: API 24+ (Android 7.0+)
- **ARCore**: Required for AR functionality
- **Camera**: Required for scanning
- **Storage**: For model files (~10MB)

## Building

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run on device
./gradlew installDebug
```

## Permissions

The app requires the following permissions:
- `CAMERA`: For AR scanning and image capture
- `WRITE_EXTERNAL_STORAGE`: For saving scan data (Android 9 and below)
- `READ_EXTERNAL_STORAGE`: For accessing scan data (Android 12 and below)

## Project Structure

```
my-ar-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/arroomscanner/app/
│   │   │   │   ├── ar/              # AR scanning components
│   │   │   │   ├── ml/              # TensorFlow Lite models
│   │   │   │   ├── models/          # Data models
│   │   │   │   ├── utils/           # Preprocessing utilities
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── ARScannerActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/          # UI layouts
│   │   │   │   ├── values/          # Strings, colors, themes
│   │   │   │   └── mipmap-*/        # App icons
│   │   │   ├── assets/
│   │   │   │   ├── models/          # TensorFlow Lite models
│   │   │   │   └── labels/          # Label files
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── java/com/arroomscanner/app/
│   │           ├── ImagePreprocessorTest.kt
│   │           └── SpatialDataPreprocessorTest.kt
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## Technologies

- **Language**: Kotlin
- **AR Framework**: ARCore 1.41.0
- **ML Framework**: TensorFlow Lite 2.14.0
- **UI**: Android Material Components
- **Camera**: CameraX 1.3.1
- **Async**: Kotlin Coroutines

## Future Enhancements

- Export scan data to common 3D formats (OBJ, PLY)
- Cloud sync for scan history
- Multi-room scanning and stitching
- Advanced material texture analysis
- Cost estimation based on detected materials
- AR visualization overlays for detected damage
- Voice-guided scanning instructions
- Integration with CAD/BIM tools

## License

This project is available for use in home improvement, construction, and design applications.

## Contributing

Contributions are welcome! Please ensure:
1. All tests pass
2. New features include tests
3. Code follows Kotlin style guidelines
4. Documentation is updated