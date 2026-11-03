# Release Strategy & CI/CD Pipeline

> **System:** NAGARLENS Infrastructure Intelligence Network  
> **Versioning Scheme:** Semantic Versioning (SemVer 2.0.0)  
> **Deployment Environments:** Development (`dev`), Staging (`stage`), Production (`prod`)  
> **Document Version:** 1.0.0  
> **Status:** Strategy Baseline  

---

## 1. Versioning Scheme

NAGARLENS releases follow strict [Semantic Versioning (SemVer 2.0.0)](https://semver.org/):

`MAJOR.MINOR.PATCH`

- **MAJOR (`M`):** Incompatible API changes, major schema restructures, or breaking system overhauls.
- **MINOR (`N`):** Backward-compatible functional features, new mobile capabilities, or new AI models.
- **PATCH (`P`):** Backward-compatible bug fixes, security patches, or documentation updates.

*Example:* `v1.2.0` (Android App & Backend API baseline compatibility release).

---

## 2. Release & Deployment Environments

```text
  +--------------------+      +--------------------+      +--------------------+
  |    DEVELOPMENT     |      |      STAGING       |      |     PRODUCTION     |
  |                    |      |                    |      |                    |
  | Branch: `main`     | ---> | Branch: `release/*`| ---> | Tag: `vX.Y.Z`      |
  | Auto-deploy to Dev |      | Staging DB & S3    |      | Production PostGIS |
  | Mocked AI Engine   |      | Production Replica |      | Live Field Traffic |
  +--------------------+      +--------------------+      +--------------------+
```

### Environment Matrix

| Environment | Purpose | Database | Android Distribution |
| :--- | :--- | :--- | :--- |
| **Development** | Continuous integration & nightly builds | Isolated PostGIS dev instance | Internal Testing APK (Firebase App Distro) |
| **Staging** | QA verification, load testing, field pilot testing | Anonymized production replica DB | Internal Beta Track / TestFlight equivalent |
| **Production** | Live municipal field operations & active asset memory | High-availability PostGIS cluster | Signed Release AAB (Google Play Internal/Production) |

---

## 3. Automated CI/CD Pipelines

Automated pipelines (GitHub Actions / GitLab CI) execute on every push and pull request.

### 3.1 Pull Request Pipeline (Pre-Merge Validation)
1. **Linting & Code Formatting:**
   - Android: `ktlint`, `detekt`
   - Python: `ruff check`, `black --check`, `mypy --strict`
2. **Automated Unit & Integration Tests:**
   - Android JVM Unit tests (`./gradlew testDebugUnitTest`)
   - Backend PyTest with PostGIS Testcontainers (`pytest tests/`)
3. **Security & Secret Scanning:**
   - Scan repository for committed credentials or private keys (`detect-secrets`).

### 3.2 Production Release Pipeline (On Release Tag `v*.*.*`)
1. **Android Application Artifact:**
   - Build optimized, signed Android App Bundle (`.aab`) with R8 code shrinking and ProGuard keep rules applied.
   - Publish mapping files (`mapping.txt`) to crash reporting services for de-obfuscation.
2. **Backend Service Container:**
   - Build multi-arch Docker container (`nagarlens-backend:vX.Y.Z`).
   - Run dry-run Alembic database migrations against staging environment.
   - Execute zero-downtime rolling update deployment (Kubernetes / ECS).

---

## 4. Rollback & Emergency Procedures

1. **Backend Database Rollback:** Every Alembic migration MUST provide a valid `downgrade()` function. In the event of a critical failure during release, database migrations can be rolled back to the prior schema version.
2. **Mobile App Feature Flags:** Critical new field features MUST be wrapped behind backend-controlled feature flags (Firebase Remote Config / custom API feature flags). In case of a defect in a newly deployed field feature, the feature can be remotely disabled instantly without requiring an emergency APK release.
