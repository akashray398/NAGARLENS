# NAGARLENS — S3 Media Cloud Storage Architecture

> **Document Version:** 1.0.0  
> **Status:** Active Storage Specification  

---

## 1. Object Storage Architecture

- **S3 Bucket**: `nagarlens-evidence-media`
- **Object Key Convention**: `evidence/{id_prefix}/{evidence_id}.jpg`
- **Presigned Upload URLs**: `POST /api/v1/observations/upload-url` issues 1-hour presigned PUT URLs directly to S3/MinIO.
- **SHA-256 Digest Validation**: Server validates client-supplied SHA-256 media digest before committing database record.
- **File Limits**: Max 50 MB per media file.
