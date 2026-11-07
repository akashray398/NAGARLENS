from fastapi import APIRouter
from app.api.v1 import ai, assets, auth, automation, health, incidents, investigations, observations, predictive, sync, temporal

api_v1_router = APIRouter()
api_v1_router.include_router(health.health_router)
api_v1_router.include_router(auth.router)
api_v1_router.include_router(ai.router)
api_v1_router.include_router(assets.router)
api_v1_router.include_router(observations.router)
api_v1_router.include_router(incidents.router)
api_v1_router.include_router(investigations.router)
api_v1_router.include_router(automation.router)
api_v1_router.include_router(predictive.router)
api_v1_router.include_router(sync.router)
api_v1_router.include_router(temporal.router)
