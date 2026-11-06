# NAGARLENS — Model Evaluation & Benchmark Metrics Framework

> **Document Version:** 1.0.0  
> **Status:** Active Benchmark Specification  

---

## 1. Evaluation Methodology

The `ModelBenchmarkEvaluator` ([`evaluator.py`](file:///D:/NAGARLENS/$HOME/Projects/NAGARLENS/backend/app/ml/evaluator.py)) evaluates computer vision detection and classification performance against ground-truth field data.

### Key Benchmark Metrics:
- **Precision**: $\frac{TP}{TP + FP}$
- **Recall**: $\frac{TP}{TP + FN}$
- **F1 Score**: $2 \times \frac{\text{Precision} \times \text{Recall}}{\text{Precision} + \text{Recall}}$
- **mAP@50**: Mean Average Precision at IoU threshold 0.50
- **mAP@50:95**: Mean Average Precision averaged across IoU thresholds from 0.50 to 0.95 (step 0.05)
- **Inference Latency**: Mean execution time in milliseconds (ms) per image frame
- **Throughput**: Frames per second (FPS)

---

## 2. Leakage-Free Dataset Split Policy

- Splitting is performed exclusively using `IncidentGroupedSplitter` ([`dataset.py`](file:///D:/NAGARLENS/$HOME/Projects/NAGARLENS/backend/app/ml/dataset.py)).
- **Rule**: All imagery belonging to the same physical incident or location group is assigned strictly to a single split (Train, Val, or Test).
- Near-duplicate images across splits are detected using 64-bit dHash perceptual hamming distance checks.
