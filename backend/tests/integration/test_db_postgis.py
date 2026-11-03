import pytest
from sqlalchemy import text
from app.infrastructure.db.session import AsyncSessionLocal


@pytest.mark.integration
@pytest.mark.asyncio
async def test_postgis_database_connectivity_and_capability() -> None:
    """Real PostGIS integration test verifying live PostgreSQL connectivity and PostGIS capability.

    REQUIRES: Running PostgreSQL 16 + PostGIS 3.4 database instance.
    """
    try:
        async with AsyncSessionLocal() as session:
            result = await session.execute(text("SELECT PostGIS_Full_Version();"))
            version_str = result.scalar()
            assert version_str is not None
            assert "POSTGIS" in str(version_str)
    except Exception as exc:
        pytest.fail(f"PostGIS Integration Test Failed: Unable to connect to PostgreSQL/PostGIS: {exc}")
