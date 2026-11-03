from fastapi import APIRouter, Response, status
from pydantic import BaseModel, Field
from sqlalchemy import text
from app.core.config import settings
from app.core.logging import logger
from app.infrastructure.db.session import AsyncSessionLocal


class HealthLiveResponse(BaseModel):
    status: str = Field(example="alive")
    environment: str = Field(example="development")
    version: str = Field(example="0.3.0")


class DatabaseComponentStatus(BaseModel):
    status: str = Field(example="healthy")
    connected: bool = Field(example=True)
    postgis_available: bool = Field(example=True)
    postgis_version: str | None = Field(default=None)


class ApiComponentStatus(BaseModel):
    status: str = Field(example="healthy")


class HealthComponents(BaseModel):
    api: ApiComponentStatus
    database: DatabaseComponentStatus


class HealthReadyResponse(BaseModel):
    status: str = Field(example="healthy")
    environment: str = Field(example="development")
    version: str = Field(example="0.3.0")
    components: HealthComponents


health_router = APIRouter(prefix="/health", tags=["Diagnostic & Health"])


@health_router.get("/live", response_model=HealthLiveResponse, status_code=status.HTTP_200_OK)
async def health_live() -> HealthLiveResponse:
    """Process liveness probe endpoint returning 200 OK when API container is running."""
    return HealthLiveResponse(
        status="alive",
        environment=settings.ENVIRONMENT,
        version=settings.VERSION,
    )


@health_router.get("/ready", response_model=HealthReadyResponse)
async def health_ready(response: Response) -> HealthReadyResponse:
    """Readiness probe checking API process, PostgreSQL connection, and PostGIS capability."""
    api_status = ApiComponentStatus(status="healthy")
    db_connected = False
    postgis_available = False
    postgis_version_str: str | None = None

    try:
        async with AsyncSessionLocal() as session:
            result = await session.execute(text("SELECT PostGIS_Full_Version();"))
            row = result.scalar()
            if row:
                db_connected = True
                postgis_available = True
                postgis_version_str = str(row)
    except Exception as exc:
        logger.warning("Health readiness check database connection failed: %s", exc)
        db_connected = False
        postgis_available = False

    db_status_str = "healthy" if (db_connected and postgis_available) else "unhealthy"
    overall_status_str = "healthy" if db_status_str == "healthy" else "unhealthy"

    if overall_status_str == "unhealthy":
        response.status_code = status.HTTP_503_SERVICE_UNAVAILABLE

    return HealthReadyResponse(
        status=overall_status_str,
        environment=settings.ENVIRONMENT,
        version=settings.VERSION,
        components=HealthComponents(
            api=api_status,
            database=DatabaseComponentStatus(
                status=db_status_str,
                connected=db_connected,
                postgis_available=postgis_available,
                postgis_version=postgis_version_str,
            ),
        ),
    )
