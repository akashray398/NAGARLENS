"""NAGARLENS — Cryptographic JWT Authentication, Password Hashing, and RBAC Dependencies."""

import bcrypt
import hashlib
from datetime import datetime, timedelta, timezone
import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel

from app.core.config import settings

# FastAPI Bearer Token Security Scheme
security_bearer = HTTPBearer(auto_error=False)


class AuthenticatedUser(BaseModel):
    user_id: str
    username: str
    email: str
    role: str  # "field_worker", "reviewer", "administrator"
    org_id: str = "org_bbmp_central"


def hash_password(password: str) -> str:
    """Hashes plaintext password using Bcrypt with salt."""
    pwd_bytes = password.encode('utf-8')[:72]  # Truncate to 72 bytes for Bcrypt standard
    salt = bcrypt.gensalt()
    return bcrypt.hashpw(pwd_bytes, salt).decode('utf-8')


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verifies plaintext password against stored Bcrypt hash."""
    pwd_bytes = plain_password.encode('utf-8')[:72]
    try:
        return bcrypt.checkpw(pwd_bytes, hashed_password.encode('utf-8'))
    except Exception:
        return False


def create_access_token(data: dict, expires_delta: timedelta | None = None) -> str:
    """Generates a short-lived cryptographic JWT Access Token."""
    to_encode = data.copy()
    now = datetime.now(timezone.utc)
    expire = now + (expires_delta or timedelta(minutes=60))
    to_encode.update({
        "exp": expire,
        "iat": now,
        "iss": settings.JWT_ISSUER,
        "aud": settings.JWT_AUDIENCE,
        "type": "access"
    })
    return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.JWT_ALGORITHM)


def create_refresh_token(data: dict) -> str:
    """Generates a long-lived JWT Refresh Token for session rotation."""
    to_encode = data.copy()
    now = datetime.now(timezone.utc)
    expire = now + timedelta(days=7)
    to_encode.update({
        "exp": expire,
        "iat": now,
        "iss": settings.JWT_ISSUER,
        "aud": settings.JWT_AUDIENCE,
        "type": "refresh"
    })
    return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.JWT_ALGORITHM)


def configure_cors(app) -> None:
    """Configures explicit Cross-Origin Resource Sharing (CORS) rules."""
    from fastapi.middleware.cors import CORSMiddleware

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.CORS_ORIGINS,
        allow_credentials=True,
        allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
        allow_headers=["*"],
    )


def decode_and_verify_jwt(token: str, expected_type: str = "access") -> AuthenticatedUser:
    """Decodes and cryptographically verifies JWT Bearer token."""
    try:
        payload = jwt.decode(
            token,
            settings.SECRET_KEY,
            algorithms=[settings.JWT_ALGORITHM],
            issuer=settings.JWT_ISSUER,
            audience=settings.JWT_AUDIENCE,
            options={
                "verify_signature": True,
                "verify_exp": True,
                "verify_iss": True,
                "verify_aud": True,
            },
        )
    except jwt.ExpiredSignatureError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication failed: JWT Bearer token has expired.",
        )
    except jwt.InvalidIssuerError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication failed: Invalid token issuer.",
        )
    except jwt.InvalidAudienceError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication failed: Invalid token audience.",
        )
    except jwt.PyJWTError as exc:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Authentication failed: Invalid or corrupt JWT signature ({str(exc)}).",
        )

    token_type = payload.get("type", "access")
    if token_type != expected_type:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Authentication failed: Expected '{expected_type}' token, got '{token_type}'.",
        )

    user_id = payload.get("sub") or payload.get("user_id")
    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication failed: Token payload missing 'sub' claim.",
        )

    role = str(payload.get("role", "field_worker")).lower().strip()
    username = str(payload.get("username") or f"user_{user_id}")
    email = str(payload.get("email") or f"{username}@nagarlens.gov")
    org_id = str(payload.get("org_id") or "org_bbmp_central")

    return AuthenticatedUser(
        user_id=str(user_id),
        username=username,
        email=email,
        role=role,
        org_id=org_id
    )


async def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(security_bearer),
) -> AuthenticatedUser:
    """FastAPI dependency enforcing cryptographic Bearer token authentication."""
    if not credentials or not credentials.credentials:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication required. Missing or empty Bearer Authorization header.",
        )

    return decode_and_verify_jwt(credentials.credentials, expected_type="access")


async def get_current_reviewer(
    user: AuthenticatedUser = Depends(get_current_user),
) -> AuthenticatedUser:
    """Enforces reviewer/administrator role authorization."""
    if user.role not in ("reviewer", "administrator", "admin"):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Forbidden: Reviewer or administrator privileges required to perform asset verification.",
        )
    return user


async def get_current_admin(
    user: AuthenticatedUser = Depends(get_current_user),
) -> AuthenticatedUser:
    """Enforces administrator role authorization."""
    if user.role not in ("administrator", "admin"):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Forbidden: Administrator privileges required.",
        )
    return user
