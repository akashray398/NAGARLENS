# NAGARLENS — Drainage Defect Taxonomy Specification

> **Taxonomy Version:** `v1.0.0`  
> **Domain:** Urban Drainage Asset Condition Assessment  
> **Scope:** Visual Defect Classification & Bounding-Box Localization  

---

## 1. Executive Summary & Governance

This document establishes the official, versioned **Drainage Defect Taxonomy** for NAGARLENS (`v1.0.0`). The taxonomy defines 9 categories to classify visually observable infrastructure defects in urban surface drainage assets (catch basins, channels, culverts, manholes).

### Key Governance Rules:
1. **Fact vs. Cause Separation:** Visually observable physical evidence (e.g. standing water, debris pile) is recorded as a physical fact. Inferred underground causes (e.g. downstream pipe collapse 50 meters away) are **never asserted** solely from surface imagery.
2. **Provisional Thresholds:** Cross-section area percentages (e.g., >30% channel blockage) are **provisional visual annotation guidelines** for human annotators, not certified hydraulic flow calculations.
3. **Human-in-the-Loop Audit:** Predictions with confidence scores < 0.85 or labeled `UNKNOWN_INSUFFICIENT` require mandatory secondary review by a certified municipal engineer.

---

## 2. Category Specification Matrix

| Category ID | Category Name | Description | Visual Indicators | Inclusion Criteria | Exclusion Criteria |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `DRAIN_BLOCKAGE` | Drain Blockage | Physical obstruction impeding water flow. | Debris pile, trapped rocks, collapsed rubble obstructing channel. | Visible channel obstruction restricting flow. | Stagnant water without visible physical debris. |
| `SOLID_WASTE` | Solid Waste Accumulation | Anthropogenic refuse dumped in drainage asset. | Plastic bottles, bags, household waste, construction trash. | Visible man-made trash accumulation in basin or channel. | Natural silt, leaves, or soil accumulation. |
| `SILT_SEDIMENT` | Silt & Sediment Accumulation | Deposition of inorganic soil, sand, or mud. | Heavy mud bed, settled sand layer filling drain invert. | Natural sediment deposition reducing effective drain depth. | Trash, plastic, or structural rubble. |
| `WATERLOGGING` | Standing Water / Waterlogging | Stagnant water pooling due to poor slope or blockage. | Pool with zero flow velocity, watermarks above invert. | Pool of stagnant water observed inside or around asset. | Fast-flowing clear water during active rainfall. |
| `STRUCTURAL_DAMAGE` | Drain Structural Damage | Physical degradation of masonry or concrete structure. | Cracks, spalling, collapsed sidewalls, displaced bricks. | Fractured or eroded concrete/masonry lining. | Missing cover slabs (categorized separately). |
| `COVER_DAMAGE_MISSING` | Drain Cover Damage / Missing | Damage to or absence of top cover slab or grate. | Missing metal grate, broken concrete top slab, exposed opening. | Damaged, displaced, or absent drain cover/grate. | Internal wall damage. |
| `DRAIN_OVERFLOW` | Drain Overflow Evidence | Evidence of water breaching channel embankments. | High-water stain lines on road, sediment spread on curb. | Visual marks or sediment trails indicating recent overtopping. | Water contained strictly inside channel walls. |
| `VEGETATION_INTRUSION` | Vegetation Intrusion | Biological growth choking the channel. | Weed growth, tree roots penetrating masonry, thick algae. | Plants or roots invading channel cross-section. | Landscaping plants outside drain perimeter. |
| `UNKNOWN_INSUFFICIENT` | Unknown / Insufficient Evidence | Quality or framing prevents definitive classification. | Motion blur, severe underexposure, finger over lens, out of frame. | Unclear image quality or asset not visible. | Clear view of drain structure. |
