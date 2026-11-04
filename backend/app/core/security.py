"""NAGARLENS — Cryptographic JWT Authentication & Role-Based Authorization Dependencies."""

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
    role: str  # "field_worker", "reviewer", "administrator"


def configure_cors(app) -> None:
    """Configures explicit Cross-Origin Resource Sharing (CORS) rules."""
    from fastapi.middleware.cors import CORSMiddleware

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.CORS_ORIGINS,
        allow_credentials=True,
        allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        allow_headers=["*"],
    )


def decode_and_verify_jwt(token: str) -> AuthenticatedUser:
    """Decodes and cryptographically verifies JWT Bearer token using secret key, algorithm, issuer, and audience."""
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

    user_id = payload.get("sub") or payload.get("user_id")
    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication failed: Token payload missing 'sub' / 'user_id' claim.",
        )

    role = str(payload.get("role", "field_worker")).lower().strip()
    username = str(payload.get("username") or f"user_{user_id}")

    return AuthenticatedUser(
        user_id=str(user_id),
        username=username,
        role=role,
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

    return decode_and_verify_jwt(credentials.credentials)


async def get_current_reviewer(
    user: AuthenticatedUser = Depends(get_current_user),
) -> AuthenticatedUser:
    """Enforces reviewer/administrator role authorization for verification workflows."""
    if user.role not in ("reviewer", "administrator", "admin"):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Forbidden: Reviewer or administrator privileges required to perform asset verification.",
        )
    return user
