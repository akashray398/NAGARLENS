# Module Boundaries & Dependency Rules

> **System:** NAGARLENS Infrastructure Intelligence Network  
> **Document Version:** 1.0.0  
> **Status:** Architecture Baseline  

---

## 1. Overview & Architectural Principles

To ensure scalability, maintainability, and clean separation of concerns, NAGARLENS enforces strict module boundaries across both the Android application and the backend service architecture.

The guiding rules are:
1. **Unidirectional Dependencies:** Dependencies must strictly flow downward from high-level features to core abstractions. Circular dependencies are strictly forbidden.
2. **Encapsulated Data Owners:** No module may directly query or modify the internal database tables or internal data structures of another module.
3. **Explicit Contracts:** Modules expose explicit API interfaces or DTOs. Internal implementation details remain private to the module.

---

## 2. Android Application Module Hierarchy

The Android codebase is organized as a multi-module Gradle project structure:

```text
                                +-------------------+
                                |       :app        |
                                +---------+---------+
                                          |
            +-----------------------------+-----------------------------+
            |                                                           |
+-----------v-----------+                                   +-----------v-----------+
| :feature:capture      |                                   | :feature:asset-memory |
+-----------+-----------+                                   +-----------+-----------+
            |                                                           |
            +-----------------------------+-----------------------------+
                                          |
            +-----------------------------+-----------------------------+
            |                             |                             |
+-----------v-----------+     +-----------v-----------+     +-----------v-----------+
| :core:location        |     | :core:network         |     | :core:database        |
+-----------+-----------+     +-----------+-----------+     +-----------+-----------+
            |                             |                             |
            +-----------------------------+-----------------------------+
                                          |
            +-----------------------------+-----------------------------+
            |                                                           |
+-----------v-----------+                                   +-----------v-----------+
| :core:model           |                                   | :core:designsystem    |
+-----------------------+                                   +-----------------------+
```

### Module Responsibilities & Allowed Dependencies

| Module Name | Purpose / Responsibility | Allowed Dependencies | Forbidden Dependencies |
| :--- | :--- | :--- | :--- |
| **`:app`** | Application entry point, Hilt dependency graph assembly, main navigation host | All `:feature:*`, `:core:*` | None |
| **`:feature:capture`** | CameraX capture screen, EXIF binding, SHA-256 computation, UI state | `:core:model`, `:core:designsystem`, `:core:location`, `:core:database` | Other `:feature:*` modules |
| **`:feature:asset-memory`**| Asset timeline UI, spatial map view, observation details, filter state | `:core:model`, `:core:designsystem`, `:core:network`, `:core:database` | Other `:feature:*` modules |
| **`:feature:sync`** | Background sync dashboard, upload progress status, manual retry trigger | `:core:model`, `:core:designsystem`, `:core:network`, `:core:database` | Other `:feature:*` modules |
| **`:core:location`** | Fused Location Provider wrappers, spatial bearing, accuracy validation | `:core:model` | Any `:feature:*`, `:core:network`, `:core:database` |
| **`:core:database`** | Room DB instance, DAOs, encrypted local SQLite tables, sync queue entities | `:core:model` | Any `:feature:*`, `:core:network` |
| **`:core:network`** | Retrofit REST clients, OkHttp sync interceptors, presigned S3 upload adapters | `:core:model` | Any `:feature:*`, `:core:database` |
| **`:core:designsystem`**| Material 3 theme definitions, outdoor high-contrast field components, icons | None (UI foundations only) | Any feature or data modules |
| **`:core:model`** | Pure Kotlin data entities, domain interfaces, enums, value objects | None (Pure Kotlin library) | Android framework dependencies, Room, Retrofit, Jetpack UI |

---

## 3. Backend Modular Monolith Structure

The backend application is contained within a single deployable service package (`app`), separated internally into explicit business domains (`app.modules.*`) and shared framework utilities (`app.core.*`).

```text
backend/app/
├── core/
│   ├── config.py             # Environment configuration & feature flags
│   ├── database.py           # SQLAlchemy / GeoAlchemy2 async session factory
│   ├── security.py           # JWT verification & password hashing
│   └── storage.py            # S3 / MinIO client wrapper
├── modules/
│   ├── assets/               # Asset management domain (catch basins, drain lines)
│   ├── observations/         # Observation ingestion & spatial evidence linking
│   ├── spatial/              # PostGIS spatial analysis & buffer joins
│   ├── ai_engine/            # AI inference job trigger & result parser
│   └── audit/                # Human review verification & change logging
```

### Backend Dependency Rules
1. `app.core` must never import from any `app.modules.*`.
2. Domain modules (e.g., `app.modules.observations`) may depend on `app.core`.
3. Inter-module calls between `app.modules.assets` and `app.modules.observations` must go through declared Service Class interfaces (e.g., `AssetQueryService`), rather than direct internal model manipulation.
4. Database tables are strictly partitioned by module domain prefixes:
   - `ast_*`: Managed by `app.modules.assets`
   - `obs_*`: Managed by `app.modules.observations`
   - `ai_*`: Managed by `app.modules.ai_engine`
   - `aud_*`: Managed by `app.modules.audit`

---

## 4. Architectural Boundary Enforcement

### 4.1 Android Gradle Dependency Check
Gradle dependency rules will be strictly checked via custom Gradle task rules and module visibility declarations (`implementation` project references). Cross-feature imports (e.g., `:feature:capture` importing `:feature:asset-memory`) will fail at compile time.

### 4.2 Python Import Linter
`import-linter` or `ruff` custom rule sets will be configured in CI/CD pipelines to enforce python backend domain boundaries, raising build failures if `app.modules.assets` directly accesses private files inside `app.modules.observations.internal`.
