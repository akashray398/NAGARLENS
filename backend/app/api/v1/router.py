from fastapi import APIRouter
from app.api.v1 import ai, assets, health

api_v1_router = APIRouter()
api_v1_router.include_router(health.health_router)
api_v1_router.include_router(ai.router)
api_v1_router.include_router(assets.router)
