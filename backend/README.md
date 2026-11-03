# NAGARLENS Backend Service

Asynchronous FastAPI modular monolith backend for the NAGARLENS Infrastructure Intelligence Network.

## Runtime Target & Stack
- **Python Version:** Python 3.12 (`requires-python = ">=3.12, <3.13"`)
- **Web Framework:** FastAPI + Uvicorn
- **Database:** PostgreSQL 16 + PostGIS 3.4
- **ORM & Migrations:** Async SQLAlchemy 2.0 (`asyncpg`) + Alembic

## Local Setup & Dependency Installation

```bash
# 1. Create Python 3.12 virtual environment
python3.12 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# 2. Upgrade pip & install dependencies in editable mode
pip install --upgrade pip
pip install -e .[dev]
```

## Running the Server Locally

```bash
# Copy example environment configuration
cp .env.example .env

# Run FastAPI development server
uvicorn app.main:app --reload --port 8000
```

## Database Migrations (Alembic)

```bash
# Execute PostGIS initialization & database migrations
alembic upgrade head
```

## Running Tests

```bash
# Run isolated unit tests (No DB required)
pytest tests/unit

# Run PostGIS integration tests (Requires active PostgreSQL/PostGIS instance)
pytest tests/integration
```
