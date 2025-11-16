# TensorFlow Lite Models

This directory contains the AI models used for material detection and damage evaluation.

## Models

### Material Detection
- `material_detector.tflite` - Primary model for detecting material types (wood, concrete, drywall, etc.)
- `material_detector_fallback.tflite` - Lighter fallback model used when primary model fails or on lower-end devices

### Damage Evaluation
- `damage_evaluator.tflite` - Primary model for detecting and evaluating damage (cracks, water damage, mold, etc.)
- `damage_evaluator_fallback.tflite` - Lighter fallback model for damage detection

## Model Specifications

### Input
- Image size: 224x224 pixels
- Color space: RGB
- Normalization: [0, 1] range

### Output
- Material Detector: 8 class probabilities (one per material type)
- Damage Evaluator: 5 damage type probabilities + 1 severity score

## Training

These models should be trained on labeled datasets of:
- Various building materials in different lighting conditions
- Different types of structural damage at varying severity levels
- Real-world room scanning scenarios

Models are optimized for on-device inference using TensorFlow Lite.
