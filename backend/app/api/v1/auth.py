"""NAGARLENS — Authentication & Identity Router."""

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel

from app.core.security import (
    AuthenticatedUser,
    create_access_token,
    create_refresh_token,
    decode_and_verify_jwt,
    get_current_user,
    hash_password,
    verify_password,
)
from app.schemas.auth import (
    LoginRequestSchema,
    RegisterRequestSchema,
    TokenResponseSchema,
    UserProfileResponseSchema,
)

router = APIRouter(prefix="/auth", tags=["Authentication & Identity"])

# In-Memory user repository fallback for sandbox testing
_USER_DB = {
    "auditor_central": {
        "user_id": "usr_dev_auditor_001",
        "username": "auditor_central",
        "email": "auditor@bbmp.gov.in",
        "full_name": "Rajesh Sharma",
        "hashed_password": hash_password("password"),
        "role": "field_worker",
        "assigned_ward": "Ward 12 — Bangalore Central",
        "org_id": "org_bbmp_central"
    }
}


class RefreshTokenRequestSchema(BaseModel):
    refresh_token: str


@router.post("/register", response_model=UserProfileResponseSchema, status_code=status.HTTP_201_CREATED, summary="Register new field auditor or inspector")
async def register_user(data: RegisterRequestSchema):
    if data.username in _USER_DB:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"User with username '{data.username}' already exists."
        )

    user_id = f"usr_{data.username.lower()}"
    user_record = {
        "user_id": user_id,
        "username": data.username,
        "email": data.email,
        "full_name": data.full_name,
        "hashed_password": hash_password(data.password),
        "role": data.role.lower(),
        "assigned_ward": data.assigned_ward,
        "org_id": "org_bbmp_central"
    }
    _USER_DB[data.username] = user_record

    return UserProfileResponseSchema(
        user_id=user_id,
        username=data.username,
        email=data.email,
        full_name=data.full_name,
        role=data.role,
        assigned_ward=data.assigned_ward,
        org_id="org_bbmp_central",
        is_active=True
    )


@router.post("/token", response_model=TokenResponseSchema, summary="Authenticate user credentials and issue JWT Access & Refresh tokens")
async def login_for_access_token(data: LoginRequestSchema):
    user = _USER_DB.get(data.username)
    if not user or not verify_password(data.password, user["hashed_password"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication failed: Invalid username or password."
        )

    token_data = {
        "sub": user["user_id"],
        "username": user["username"],
        "email": user["email"],
        "role": user["role"],
        "org_id": user["org_id"]
    }

    access_token = create_access_token(token_data)
    refresh_token = create_refresh_token(token_data)

    return TokenResponseSchema(
        access_token=access_token,
        refresh_token=refresh_token,
        expires_in_seconds=3600,
        user_id=user["user_id"],
        role=user["role"]
    )


@router.post("/refresh", response_model=TokenResponseSchema, summary="Rotate expired JWT Access token using Refresh token")
async def refresh_access_token(data: RefreshTokenRequestSchema):
    user_payload = decode_and_verify_jwt(data.refresh_token, expected_type="refresh")

    token_data = {
        "sub": user_payload.user_id,
        "username": user_payload.username,
        "email": user_payload.email,
        "role": user_payload.role,
        "org_id": user_payload.org_id
    }

    new_access_token = create_access_token(token_data)
    new_refresh_token = create_refresh_token(token_data)

    return TokenResponseSchema(
        access_token=new_access_token,
        refresh_token=new_refresh_token,
        expires_in_seconds=3600,
        user_id=user_payload.user_id,
        role=user_payload.role
    )


@router.get("/me", response_model=UserProfileResponseSchema, summary="Retrieve current authenticated user profile")
async def get_my_profile(current_user: AuthenticatedUser = Depends(get_current_user)):
    user = _USER_DB.get(current_user.username)
    full_name = user["full_name"] if user else current_user.username.replace("_", " ").title()
    ward = user["assigned_ward"] if user else "Ward 12 — Bangalore Central"

    return UserProfileResponseSchema(
        user_id=current_user.user_id,
        username=current_user.username,
        email=current_user.email,
        full_name=full_name,
        role=current_user.role,
        assigned_ward=ward,
        org_id=current_user.org_id,
        is_active=True
    )
