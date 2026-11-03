# Testing Strategy & Verification Baseline

> **System:** NAGARLENS Infrastructure Intelligence Network  
> **Document Version:** 1.0.0  
> **Status:** Strategy Baseline  

---

## 1. Testing Philosophy & Verification Principles

As a high-integrity physical infrastructure network, system correctness is non-negotiable. Tests are required at every architectural layer to verify data persistence, evidence anti-tampering, spatial logic, and offline sync resilience.

### Verification Rules
1. **Zero Guesswork:** Never claim tests pass without executing test suites.
2. **Automated CI Integration:** All PRs must pass unit, integration, and linting suites prior to merge.
3. **Deterministic Fakes:** Prefer fast, in-memory fake implementations for network/database boundaries during unit tests over complex mock trees.

---

## 2. Android Mobile Testing Strategy

```text
       / \
      / UI\          5%  - Jetpack Compose UI Tests (Robolectric / Espresso)
     /-----\
    / Integ \       25% - Integration Tests (Room DB, WorkManager, Hilt)
   /---------\
  /   Unit    \     70% - Unit Tests (ViewModels, UseCases, Repositories)
 /-------------\
```

### 2.1 Android Unit Tests (JUnit 5 + MockK + Coroutines Test)
- **Scope:** Business logic inside `UseCases`, `ViewModels`, SHA-256 hash calculation, EXIF parser wrappers.
- **Rules:** Tests must run locally on JVM without an Android emulator in under 15 seconds for the entire suite.
- **Key Test Case:** Verify that modifying a single byte in captured media invalidates the calculated SHA-256 digest payload.

### 2.2 Android Integration & Room Database Tests
- **Scope:** Room DAOs, encrypted SQLite queries, migration scripts, WorkManager offline queue synchronization.
- **Tooling:** `androidx.room:room-testing`, `Robolectric` for fast JVM execution.

### 2.3 Compose UI Tests
- **Scope:** High-contrast field component styling, status badge rendering, camera framing overlay, thumb-zone layout bounds.

---

## 3. Backend Testing Strategy (Python FastAPI)

### 3.1 Backend Unit Tests (PyTest)
- **Scope:** Domain models, data validation schemas, cryptography helpers, business rules.
- **Execution:** Runs in pure Python memory without external database or S3 dependencies.

### 3.2 PostGIS Integration Tests (PyTest + Testcontainers)
- **Scope:** REST route handlers, PostGIS spatial queries (`ST_DWithin`, spatial joins), Alembic database migrations.
- **Infrastructure:** Uses **Testcontainers Python** to spin up a real `postgis/postgis:16-3.4` Docker container for isolated, deterministic spatial test runs.

```python
# Example Spatial Integration Test Structure
@pytest.mark.asyncio
async def test_find_nearest_asset_within_5m(db_session):
    # Insert test drain asset at precise coordinates
    # Query spatial API endpoint with location offset 2 meters away
    # Assert correct asset ID is returned with expected distance
    pass
```

---

## 4. AI Vision Pipeline Testing

1. **Synthetic & Golden Evaluation Datasets:** AI models are validated against a curated, human-annotated benchmark dataset of urban drainage defects (1,000+ labeled field images).
2. **Regression Metrics:** Every AI model iteration must meet minimum accuracy thresholds before deployment:
   - Defect Classification Precision: `>= 92%`
   - Defect Classification Recall: `>= 88%`
   - Face & Plate Privacy Blurring Accuracy: `>= 99.5%`
3. **Deterministic Visual Tests:** Synthetic images with known face/plate patterns are run through the privacy blurring module to verify automated pixel obscuration.

---

## 5. Offline Sync & Network Fault Injection Testing

To guarantee NFR compliance under poor field connectivity:
1. **Network Interruption Tests:** Test upload jobs interrupted mid-transfer, verifying chunk resumption upon reconnect without payload corruption.
2. **Database Crash Recovery:** Simulate sudden Android process termination while saving an observation; verify local Room database integrity remains uncorrupted.
