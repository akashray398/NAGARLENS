"""NAGARLENS — Unit Tests for Dataset Architecture and Leakage Prevention."""

from app.ml.dataset import IncidentGroupedSplitter, ImageRecord, BoundingBox
from app.ml.taxonomy import DefectCategory


def test_incident_grouped_splitter_prevents_leakage():
    records = []
    # Create 10 incidents with 3 images each (30 images total)
    for incident_idx in range(10):
        incident_id = f"INC_GROUP_{incident_idx}"
        for img_idx in range(3):
            records.append(
                ImageRecord(
                    image_id=f"IMG_{incident_idx}_{img_idx}",
                    evidence_id=f"EVID_{incident_idx}_{img_idx}",
                    incident_id=incident_id,
                    location_group_id=f"LOC_{incident_idx}",
                    file_path=f"/data/img_{incident_idx}_{img_idx}.jpg",
                    sha256_hash="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                    width=640,
                    height=480,
                    primary_category=DefectCategory.SOLID_WASTE,
                )
            )

    manifest = IncidentGroupedSplitter.partition_dataset(
        records=records,
        train_ratio=0.70,
        val_ratio=0.15,
        test_ratio=0.15,
        seed=42,
    )

    train_set = set(manifest.train_ids)
    val_set = set(manifest.val_ids)
    test_set = set(manifest.test_ids)

    # 1. No overlap between train, val, and test image IDs
    assert train_set.isdisjoint(val_set)
    assert train_set.isdisjoint(test_set)
    assert val_set.isdisjoint(test_set)

    # 2. Verify all images from the same incident belong to the SAME split
    incident_splits: dict[str, str] = {}
    for record in records:
        if record.image_id in train_set:
            split = "TRAIN"
        elif record.image_id in val_set:
            split = "VAL"
        else:
            split = "TEST"

        if record.incident_id in incident_splits:
            assert incident_splits[record.incident_id] == split, f"Leakage detected for incident {record.incident_id}"
        else:
            incident_splits[record.incident_id] = split
