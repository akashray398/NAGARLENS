"""NAGARLENS — Model Evaluation Metrics & Class Weighting Foundations."""

from typing import Dict, List
from pydantic import BaseModel

from app.ml.taxonomy import DefectCategory


class EvaluationMetrics(BaseModel):
    per_class_precision: Dict[str, float]
    per_class_recall: Dict[str, float]
    per_class_f1: Dict[str, float]
    overall_macro_f1: float
    confusion_matrix: Dict[str, Dict[str, int]]


class ModelEvaluator:
    """Evaluates classifier predictions and generates per-class metrics.

    NOTE: Test-set data is strictly reserved for final evaluation reporting
    and is NEVER used for model tuning, threshold selection, or loss weighting.
    """

    @staticmethod
    def evaluate_predictions(
        y_true: List[DefectCategory],
        y_pred: List[DefectCategory]
    ) -> EvaluationMetrics:
        categories = [c.name for c in DefectCategory]

        # Initialize confusion matrix
        cm: Dict[str, Dict[str, int]] = {
            c_true: {c_pred: 0 for c_pred in categories} for c_true in categories
        }

        for true_cat, pred_cat in zip(y_true, y_pred):
            cm[true_cat.name][pred_cat.name] += 1

        precision: Dict[str, float] = {}
        recall: Dict[str, float] = {}
        f1: Dict[str, float] = {}

        for cat in categories:
            tp = cm[cat][cat]
            fp = sum(cm[c_true][cat] for c_true in categories if c_true != cat)
            fn = sum(cm[cat][c_pred] for c_pred in categories if c_pred != cat)

            prec = tp / (tp + fp) if (tp + fp) > 0 else 0.0
            rec = tp / (tp + fn) if (tp + fn) > 0 else 0.0
            f1_val = (2 * prec * rec) / (prec + rec) if (prec + rec) > 0 else 0.0

            precision[cat] = round(prec, 4)
            recall[cat] = round(rec, 4)
            f1[cat] = round(f1_val, 4)

        non_zero_f1s = [v for v in f1.values() if v > 0.0]
        macro_f1 = round(sum(f1.values()) / len(categories), 4) if categories else 0.0

        return EvaluationMetrics(
            per_class_precision=precision,
            per_class_recall=recall,
            per_class_f1=f1,
            overall_macro_f1=macro_f1,
            confusion_matrix=cm,
        )


class ClassWeightCalculator:
    """Calculates inverse frequency class weights to address training dataset class imbalance."""

    @staticmethod
    def calculate_weights(labels: List[DefectCategory]) -> Dict[str, float]:
        counts: Dict[str, int] = {}
        total = len(labels)
        if total == 0:
            return {c.name: 1.0 for c in DefectCategory}

        for label in labels:
            counts[label.name] = counts.get(label.name, 0) + 1

        num_classes = len(DefectCategory)
        weights: Dict[str, float] = {}

        for c in DefectCategory:
            c_count = counts.get(c.name, 0)
            if c_count > 0:
                weights[c.name] = round(total / (num_classes * c_count), 4)
            else:
                weights[c.name] = 1.0

        return weights
