"""NAGARLENS — Reproducible Dataset Architecture & Grouped Split Generator."""

import random
from dataclasses import field
from typing import Dict, List, Optional, Tuple
from pydantic import BaseModel

from app.ml.taxonomy import DefectCategory


class BoundingBox(BaseModel):
    category: DefectCategory
    confidence: float = 1.0
    x_min: float
    y_min: float
    x_max: float
    y_max: float


class ImageRecord(BaseModel):
    image_id: str
    evidence_id: str
    incident_id: Optional[str] = None
    location_group_id: Optional[str] = None
    file_path: str
    sha256_hash: str
    dhash: Optional[str] = None
    width: int
    height: int
    quality_status: str = "PASS"
    primary_category: DefectCategory
    bounding_boxes: List[BoundingBox] = field(default_factory=list)


class DatasetManifest(BaseModel):
    dataset_id: str
    version: str = "v1.0.0"
    train_ids: List[str] = field(default_factory=list)
    val_ids: List[str] = field(default_factory=list)
    test_ids: List[str] = field(default_factory=list)
    records: List[ImageRecord] = field(default_factory=list)
    actual_ratios: Dict[str, float] = field(default_factory=dict)


def hamming_distance(hex_hash1: str, hex_hash2: str) -> int:
    """Calculates Hamming distance between two hex-encoded perceptual hashes."""
    if not hex_hash1 or not hex_hash2 or len(hex_hash1) != len(hex_hash2):
        return 64  # Maximum distance for invalid/mismatched hashes
    try:
        val1 = int(hex_hash1, 16)
        val2 = int(hex_hash2, 16)
        return bin(val1 ^ val2).count("1")
    except ValueError:
        return 64


class IncidentGroupedSplitter:
    """Incident/Location Grouped Split Generator preventing data leakage across splits."""

    @staticmethod
    def partition_dataset(
        records: List[ImageRecord],
        train_ratio: float = 0.70,
        val_ratio: float = 0.15,
        test_ratio: float = 0.15,
        seed: int = 42,
    ) -> DatasetManifest:
        assert abs((train_ratio + val_ratio + test_ratio) - 1.0) < 1e-5, "Target ratios must sum to 1.0"

        if not records:
            return DatasetManifest(
                dataset_id=f"NAGARLENS_DS_{seed}",
                version="v1.0.0",
                train_ids=[],
                val_ids=[],
                test_ids=[],
                records=[],
                actual_ratios={"train": 0.0, "val": 0.0, "test": 0.0},
            )

        # Safely resolve group key: incident_id -> location_group_id -> image_id fallback
        grouped: Dict[str, List[ImageRecord]] = {}
        for record in records:
            key = None
            if record.incident_id and record.incident_id.strip():
                key = f"INC_{record.incident_id.strip()}"
            elif record.location_group_id and record.location_group_id.strip():
                key = f"LOC_{record.location_group_id.strip()}"
            else:
                key = f"IMG_{record.image_id}"

            grouped.setdefault(key, []).append(record)

        group_keys = list(grouped.keys())
        rng = random.Random(seed)
        rng.shuffle(group_keys)

        total_groups = len(group_keys)
        n_train = max(1, int(total_groups * train_ratio)) if total_groups >= 3 else total_groups
        n_val = int(total_groups * val_ratio) if total_groups >= 3 else 0

        train_groups = set(group_keys[:n_train])
        val_groups = set(group_keys[n_train : n_train + n_val])
        test_groups = set(group_keys[n_train + n_val :])

        train_ids: List[str] = []
        val_ids: List[str] = []
        test_ids: List[str] = []

        for key, recs in grouped.items():
            ids = [r.image_id for r in recs]
            if key in train_groups:
                train_ids.extend(ids)
            elif key in val_groups:
                val_ids.extend(ids)
            else:
                test_ids.extend(ids)

        total_records = len(records)
        actual_ratios = {
            "train": round(len(train_ids) / total_records, 4) if total_records > 0 else 0.0,
            "val": round(len(val_ids) / total_records, 4) if total_records > 0 else 0.0,
            "test": round(len(test_ids) / total_records, 4) if total_records > 0 else 0.0,
        }

        return DatasetManifest(
            dataset_id=f"NAGARLENS_DS_{seed}",
            version="v1.0.0",
            train_ids=train_ids,
            val_ids=val_ids,
            test_ids=test_ids,
            records=records,
            actual_ratios=actual_ratios,
        )

    @staticmethod
    def find_cross_split_near_duplicates(
        manifest: DatasetManifest,
        distance_threshold: int = 10,
    ) -> List[Tuple[str, str, int]]:
        """Identifies near-duplicate images across splits using perceptual hash dHash distance."""
        record_map = {r.image_id: r for r in manifest.records if r.dhash}
        near_duplicates: List[Tuple[str, str, int]] = []

        split_pairs = [
            (manifest.train_ids, manifest.val_ids, "train_vs_val"),
            (manifest.train_ids, manifest.test_ids, "train_vs_test"),
            (manifest.val_ids, manifest.test_ids, "val_vs_test"),
        ]

        for set1_ids, set2_ids, _ in split_pairs:
            for id1 in set1_ids:
                r1 = record_map.get(id1)
                if not r1 or not r1.dhash:
                    continue
                for id2 in set2_ids:
                    r2 = record_map.get(id2)
                    if not r2 or not r2.dhash:
                        continue
                    dist = hamming_distance(r1.dhash, r2.dhash)
                    if dist <= distance_threshold:
                        near_duplicates.append((id1, id2, dist))

        return near_duplicates
