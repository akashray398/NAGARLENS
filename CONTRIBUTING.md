# Contributing Guidelines

Thank you for contributing to NAGARLENS. As an infrastructure intelligence platform handling physical world asset memory, high standards for evidence integrity, code quality, architectural consistency, and test coverage are strictly enforced.

---

## 1. Branching & Git Workflow

We follow a **Trunk-Based / Short-Lived Feature Branch** workflow.

### Branch Naming Conventions
- `feature/<short-description>`: New functional features (e.g., `feature/capture-offline-queue`)
- `fix/<short-description>`: Bug fixes (e.g., `fix/postgis-spatial-index`)
- `refactor/<short-description>`: Internal restructuring without functional changes
- `docs/<short-description>`: Documentation additions or updates
- `chore/<short-description>`: Maintenance, CI/CD, dependency updates

---

## 2. Commit Message Format

We strictly enforce [Conventional Commits](https://www.conventionalcommits.org/):

```text
<type>(<scope>): <short summary>

[optional body]

[optional footer(s)]
```

### Supported Types
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation changes only
- `style`: Formatting, missing semi-colons, no code change
- `refactor`: Refactoring production code
- `test`: Adding missing tests or correcting existing tests
- `chore`: Updating build tasks, package manager configs

### Examples
- `feat(android/capture): implement camera EXIF location binding`
- `fix(backend/spatial): correct spatial join query for drain catchment bounding box`
- `docs(adr): update ADR-001 with storage strategy rationale`

---

## 3. Pull Request (PR) Expectations

1. **Scope:** PRs should be atomic and focused on a single logical change.
2. **Title:** Follow conventional commit format.
3. **Description:** Include context, summary of changes, verification performed, and issue links.
4. **Checks:** All automated tests, linters, and type checks must pass before review.
5. **Reviewers:** At least one mandatory review approval from a core module maintainer.
6. **No Force Pushing to `main`:** `main` branch is protected.

---

## 4. Coding Standards

### Android (Kotlin)
- Follow official Kotlin coding conventions and Android Kotlin style guide.
- UI built exclusively with Jetpack Compose.
- Strict separation: `UI Layer` -> `ViewModel` -> `UseCase` -> `Repository` -> `Data Source`.
- Zero direct database or network access inside UI or ViewModels.

### Backend (Python)
- Python 3.12+ with type hints required for all function signatures (`mypy` strict mode).
- Formatted using `ruff` and `black`.
- FastAPI route handlers must remain thin; core business logic resides inside domain services.

### Database (PostgreSQL / PostGIS)
- All spatial coordinates must use WGS84 (`SRID 4326`) unless project-specific projected coordinates (`SRID 3857`) are required for precise planar distance calculation.
- Every schema change must include an Alembic migration file.

---

## 5. Architectural Decision Records (ADRs)

Any non-trivial architectural change, structural refactoring, or third-party dependency introduction requires an ADR entry under `docs/architecture/adr/`.

Follow the standard template:
1. **Title & Number** (`ADR-XXX-<name>.md`)
2. **Status** (Proposed / Accepted / Superseded / Rejected)
3. **Context & Problem Statement**
4. **Decision Drivers & Options Considered**
5. **Decision Outcome & Consequences**

---

## 6. Security & Sensitive Information

- **NEVER** commit credentials, API keys, private keys, database passwords, or `.env` files.
- Run pre-commit secret scans (`detect-secrets` / `trufflehog`) before pushing code.
- Report security vulnerabilities directly to the security lead; do not open public issues for zero-day vulnerabilities.
