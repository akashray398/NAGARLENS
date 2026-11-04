from contextlib import asynccontextmanager
from typing import Any
from fastapi import FastAPI
from app.api.v1.router import api_v1_router
from app.core.config import settings
from app.core.logging import logger, setup_logging
from app.core.security import configure_cors


@asynccontextmanager
async def lifespan(app: FastAPI) -> Any:
    """FastAPI application lifespan context manager for startup & shutdown tasks."""
    setup_logging()
    logger.info("Initializing %s v%s in [%s] mode", settings.PROJECT_NAME, settings.VERSION, settings.ENVIRONMENT)
    yield
    logger.info("Shutting down %s backend engine", settings.PROJECT_NAME)


def create_application() -> FastAPI:
    """Factory function initializing the NAGARLENS FastAPI backend application."""
    app = FastAPI(
        title=f"{settings.PROJECT_NAME} Infrastructure Intelligence Network",
        description="Persistent, evidence-backed memory of physical infrastructure.",
        version=settings.VERSION,
        lifespan=lifespan,
        docs_url="/docs" if settings.ENVIRONMENT != "production" else None,
        redoc_url="/redoc" if settings.ENVIRONMENT != "production" else None,
    )

    configure_cors(app)
    app.include_router(api_v1_router, prefix="/api/v1")

    # Root health convenience redirect / endpoint
    @app.get("/health", include_in_schema=False)
    async def root_health_redirect() -> dict[str, str]:
        return {
            "service": settings.PROJECT_NAME,
            "status": "alive",
            "readiness_endpoint": "/api/v1/health/ready",
            "liveness_endpoint": "/api/v1/health/live",
        }

    return app


app = create_application()
