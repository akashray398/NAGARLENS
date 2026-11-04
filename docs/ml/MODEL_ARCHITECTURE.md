# NAGARLENS — Computer Vision Model Architecture & Licensing Governance Specification

> **Architecture Status:** Proposed Baseline Architecture (Awaiting Field Dataset & Training Execution)  
> **Target Inference Runtime:** Android LiteRT (TensorFlow Lite) & PyTorch ONNX Engine  

---

## 1. Architectural Classification & Status Distinction

To maintain strict engineering honesty, model candidates are categorized across three operational tiers:

| Model Tier | Definition | Current Status in NAGARLENS |
| :--- | :--- | :--- |
| **Proposed Architecture** | Theoretical architecture evaluated for suitability based on parameter size, compute complexity, and quantization support. | **ACTIVE (MobileNetV3-Small & RT-DETR / MobileNet-SSD)** |
| **Measured Benchmark** | Empirical latency and memory usage measured on actual field hardware using validated weights. | *Pending field dataset training execution.* |
| **Validated Production Model** | Deployed model meeting >85% mAP accuracy and safety criteria on independent field test splits. | *Pending model artifact deployment.* |

---

## 2. Model Architecture Options & Use-Case Separation

1. **Image Classification (MobileNetV3-Small / EfficientNet-Lite0):**
   - **Use Case:** Global image-level triage (e.g., determining overall asset condition: `WATERLOGGING` vs `NORMAL_FLOW`).
   - **Footprint:** ~2.5M parameters (<10 MB binary size).
   - **License:** Apache 2.0 (Fully permissive for commercial redistribution).

2. **Object Detection (RT-DETR / MobileNetV3-SSD vs YOLOv8-Nano):**
   - **Use Case:** Spatial bounding-box localization of localized physical defects (e.g., pinpointing `SOLID_WASTE` clusters or `COVER_DAMAGE_MISSING` openings).
   - **Footprint:** ~3.2M parameters.

3. **Pixel Segmentation (Lite-HRNet / Mobile-DeepLabV3):**
   - **Use Case:** Pixel-exact area measurement of silt accumulation or water volume inside drain channels.

---

## 3. Licensing & Commercial Redistribution Governance

> [!IMPORTANT]
> **Ultralytics YOLOv8 Licensing Investigation:**  
> Ultralytics YOLOv8 is published under the **AGPL-3.0 Copyleft License**. Including YOLOv8 in proprietary commercial software requires purchasing an expensive commercial license from Ultralytics or releasing the entire application source code under AGPL-3.0.  
> **NAGARLENS Production Policy:** To maintain commercial redistribution freedom without copyleft contagion, NAGARLENS selects **RT-DETR-Nano (Baidu)** or **MobileNetV3-SSD** (Apache 2.0 / MIT licenses) for production object detection dependencies.

---

## 4. Quantization & On-Device Deployment

- **Format:** FlatBuffers `.tflite` for Android LiteRT, ONNX for server inference.
- **Quantization Strategy:** Post-Training INT8 Quantization (weights and activations) to reduce APK binary footprint to <5 MB while leveraging Android NNAPI hardware acceleration.
