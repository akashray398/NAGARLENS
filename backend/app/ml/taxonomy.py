"""NAGARLENS — Versioned Drainage Defect Taxonomy Module."""

from dataclasses import dataclass
from enum import Enum


class DefectCategory(str, Enum):
    DRAIN_BLOCKAGE = "DRAIN_BLOCKAGE"
    SOLID_WASTE = "SOLID_WASTE"
    SILT_SEDIMENT = "SILT_SEDIMENT"
    WATERLOGGING = "WATERLOGGING"
    STRUCTURAL_DAMAGE = "STRUCTURAL_DAMAGE"
    COVER_DAMAGE_MISSING = "COVER_DAMAGE_MISSING"
    DRAIN_OVERFLOW = "DRAIN_OVERFLOW"
    VEGETATION_INTRUSION = "VEGETATION_INTRUSION"
    UNKNOWN_INSUFFICIENT = "UNKNOWN_INSUFFICIENT"


@dataclass(frozen=True)
class CategorySpec:
    category_id: DefectCategory
    name: str
    description: str
    inclusion_criteria: str
    exclusion_criteria: str


TAXONOMY_VERSION = "v1.0.0"

TAXONOMY_REGISTRY: dict[DefectCategory, CategorySpec] = {
    DefectCategory.DRAIN_BLOCKAGE: CategorySpec(
        category_id=DefectCategory.DRAIN_BLOCKAGE,
        name="Drain Blockage",
        description="Physical obstruction impeding water flow.",
        inclusion_criteria="Debris, stones, or collapsed material blocking channel (Provisional visual guideline).",
        exclusion_criteria="Stagnant water without visible physical debris.",
    ),
    DefectCategory.SOLID_WASTE: CategorySpec(
        category_id=DefectCategory.SOLID_WASTE,
        name="Solid Waste Accumulation",
        description="Anthropogenic trash dumped in drainage asset.",
        inclusion_criteria="Plastic bottles, bags, household waste, construction trash.",
        exclusion_criteria="Natural silt, mud, or leaves.",
    ),
    DefectCategory.SILT_SEDIMENT: CategorySpec(
        category_id=DefectCategory.SILT_SEDIMENT,
        name="Silt & Sediment Accumulation",
        description="Deposition of inorganic soil, sand, or mud.",
        inclusion_criteria="Natural sediment settling filling drain invert.",
        exclusion_criteria="Trash or structural rubble.",
    ),
    DefectCategory.WATERLOGGING: CategorySpec(
        category_id=DefectCategory.WATERLOGGING,
        name="Standing Water / Waterlogging",
        description="Stagnant water pooling due to poor slope or blockage.",
        inclusion_criteria="Stagnant pool of water inside or around asset.",
        exclusion_criteria="Fast-flowing clear water during rainfall.",
    ),
    DefectCategory.STRUCTURAL_DAMAGE: CategorySpec(
        category_id=DefectCategory.STRUCTURAL_DAMAGE,
        name="Drain Structural Damage",
        description="Physical degradation of masonry or concrete structure.",
        inclusion_criteria="Cracks, spalling, collapsed sidewalls, displaced bricks.",
        exclusion_criteria="Missing cover slabs.",
    ),
    DefectCategory.COVER_DAMAGE_MISSING: CategorySpec(
        category_id=DefectCategory.COVER_DAMAGE_MISSING,
        name="Drain Cover Damage / Missing",
        description="Damage to or absence of top cover slab or grate.",
        inclusion_criteria="Missing metal grate, broken concrete top slab, exposed opening.",
        exclusion_criteria="Internal wall damage.",
    ),
    DefectCategory.DRAIN_OVERFLOW: CategorySpec(
        category_id=DefectCategory.DRAIN_OVERFLOW,
        name="Drain Overflow Evidence",
        description="Evidence of water breaching channel embankments.",
        inclusion_criteria="Visual marks or sediment trails indicating recent overtopping.",
        exclusion_criteria="Water contained strictly inside channel walls.",
    ),
    DefectCategory.VEGETATION_INTRUSION: CategorySpec(
        category_id=DefectCategory.VEGETATION_INTRUSION,
        name="Vegetation Intrusion",
        description="Biological growth choking the channel.",
        inclusion_criteria="Plants or roots invading channel cross-section (Provisional visual guideline).",
        exclusion_criteria="Landscaping plants outside drain perimeter.",
    ),
    DefectCategory.UNKNOWN_INSUFFICIENT: CategorySpec(
        category_id=DefectCategory.UNKNOWN_INSUFFICIENT,
        name="Unknown / Insufficient Evidence",
        description="Quality or framing prevents definitive classification.",
        inclusion_criteria="Motion blur, underexposure, finger over lens, or unidentifiable.",
        exclusion_criteria="Clear view of drain structure.",
    ),
}


def get_category_spec(category: DefectCategory) -> CategorySpec:
    return TAXONOMY_REGISTRY[category]
