"""NAGARLENS — Unit Tests for Model Training Metrics & Class Weighting."""

from app.ml.taxonomy import DefectCategory
from app.ml.training.trainer import ModelEvaluator, ClassWeightCalculator


def test_model_evaluator_computes_metrics_accurately():
    y_true = [
        DefectCategory.SOLID_WASTE,
        DefectCategory.SOLID_WASTE,
        DefectCategory.WATERLOGGING,
        DefectCategory.STRUCTURAL_DAMAGE,
    ]
    y_pred = [
        DefectCategory.SOLID_WASTE,
        DefectCategory.WATERLOGGING,  # Mistake
        DefectCategory.WATERLOGGING,
        DefectCategory.STRUCTURAL_DAMAGE,
    ]

    metrics = ModelEvaluator.evaluate_predictions(y_true, y_pred)

    assert metrics.per_class_precision["SOLID_WASTE"] == 1.0  # 1/1
    assert metrics.per_class_recall["SOLID_WASTE"] == 0.5     # 1/2
    assert metrics.per_class_f1["SOLID_WASTE"] == 0.6667
    assert metrics.overall_macro_f1 > 0.0


def test_class_weight_calculator_handles_imbalance():
    labels = [
        DefectCategory.SOLID_WASTE,
        DefectCategory.SOLID_WASTE,
        DefectCategory.SOLID_WASTE,
        DefectCategory.WATERLOGGING,
    ]

    weights = ClassWeightCalculator.calculate_weights(labels)

    assert weights["WATERLOGGING"] > weights["SOLID_WASTE"]
