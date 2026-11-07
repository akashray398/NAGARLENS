"""NAGARLENS — Authentication DTO Schemas."""

from typing import Optional
from pydantic import BaseModel, EmailStr, Field


class LoginRequestSchema(BaseModel):
    username: str = Field(..., description="User login handle or badge ID")
    password: str = Field(..., description="Plaintext password")


class RegisterRequestSchema(BaseModel):
    username: str = Field(..., min_length=3, max_length=64)
    email: EmailStr
    password: str = Field(..., min_length=6, max_length=128)
    full_name: str = Field(..., min_length=2, max_length=128)
    role: str = Field(default="field_worker", description="field_worker, reviewer, administrator")
    assigned_ward: str = Field(default="Ward 12 — Bangalore Central")


class TokenResponseSchema(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in_seconds: int = 3600
    user_id: str
    role: str


class UserProfileResponseSchema(BaseModel):
    user_id: str
    username: str
    email: str
    full_name: str
    role: str
    assigned_ward: str
    org_id: str
    is_active: bool = True
