"""Unit tests for S3 Media Object Storage Service."""

import hashlib
from app.core.storage import S3MediaStorageService


def test_save_media_bytes_and_sha256_validation():
    storage = S3MediaStorageService(local_storage_dir="storage/test_media")
    sample_bytes = b"SAMPLE_IMAGE_BINARY_BYTES_FOR_UNIT_TEST"
    expected_hash = hashlib.sha256(sample_bytes).hexdigest()

    result = storage.save_media_bytes(
        evidence_id="evid_unit_test_99",
        media_bytes=sample_bytes,
        expected_sha256=expected_hash
    )

    assert result.is_valid is True
    assert result.sha256_hash == expected_hash
    assert result.file_size_bytes == len(sample_bytes)


def test_save_media_bytes_sha256_mismatch():
    storage = S3MediaStorageService(local_storage_dir="storage/test_media")
    sample_bytes = b"CORRUPT_BYTES"

    result = storage.save_media_bytes(
        evidence_id="evid_unit_test_100",
        media_bytes=sample_bytes,
        expected_sha256="0000000000000000000000000000000000000000000000000000000000000000"
    )

    assert result.is_valid is False
    assert "SHA-256 hash mismatch" in result.error_message
