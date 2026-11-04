"""NAGARLENS — Unit Tests for Drainage Defect Taxonomy."""

from app.ml.taxonomy import TAXONOMY_REGISTRY, TAXONOMY_VERSION, DefectCategory, get_category_spec


def test_taxonomy_has_nine_categories():
    assert len(DefectCategory) == 9
    assert len(TAXONOMY_REGISTRY) == 9


def test_taxonomy_version_is_valid():
    assert TAXONOMY_VERSION == "v1.0.0"


def test_taxonomy_specs_are_complete():
    for category in DefectCategory:
        spec = get_category_spec(category)
        assert spec.category_id == category
        assert len(spec.name) > 0
        assert len(spec.description) > 0
        assert len(spec.inclusion_criteria) > 0
        assert len(spec.exclusion_criteria) > 0
