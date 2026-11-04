import json
from typing import Any, Self
from pydantic import Field, field_validator, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        case_sensitive=True,
    )

    # Core Application Configuration
    PROJECT_NAME: str = "NAGARLENS"
    VERSION: str = "0.3.0"
    ENVIRONMENT: str = Field(
        default="development",
        description="Runtime environment (development, staging, production)",
    )
    LOG_LEVEL: str = Field(default="INFO", description="Logging verbosity level")

    # Security & Cryptographic Authentication
    SECRET_KEY: str = Field(
        default="dev_secret_key_minimum_32_chars_long_for_security_compliance",
        description="Cryptographic secret key for signing tokens",
    )
    JWT_ALGORITHM: str = Field(default="HS256", description="JWT signing algorithm")
    JWT_ISSUER: str = Field(default="nagarlens-auth", description="Trusted JWT token issuer")
    JWT_AUDIENCE: str = Field(default="nagarlens-api", description="Trusted JWT token audience")

    CORS_ORIGINS: list[str] = Field(
        default=["http://localhost", "http://localhost:8000", "http://localhost:3000"],
        description="Allowed CORS origin domains",
    )

    # PostgreSQL + PostGIS Asynchronous & Synchronous Connection URLs
    DATABASE_URL: str = Field(
        default="postgresql+asyncpg://nagarlens_user:nagarlens_dev_secret_change_me@localhost:5432/nagarlens_db",
        description="Async SQLAlchemy database connection string",
    )
    DATABASE_URL_SYNC: str = Field(
        default="postgresql://nagarlens_user:nagarlens_dev_secret_change_me@localhost:5432/nagarlens_db",
        description="Synchronous database connection string for Alembic migrations",
    )

    @field_validator("CORS_ORIGINS", mode="before")
    @classmethod
    def parse_cors_origins(cls, v: Any) -> list[str]:
        if isinstance(v, str):
            if v.startswith("[") and v.endswith("]"):
                return json.loads(v)
            return [origin.strip() for origin in v.split(",") if origin.strip()]
        if isinstance(v, list):
            return v
        return ["http://localhost", "http://localhost:8000"]

    @model_validator(mode="after")
    def validate_production_security(self) -> Self:
        known_dev_secrets = {
            "dev_secret_key_minimum_32_chars_long_for_security_compliance",
            "changeme",
            "secret",
            "password",
            "dev_secret",
            "123456",
        }
        if self.ENVIRONMENT.lower() in ["production", "prod", "staging"]:
            if not self.SECRET_KEY or self.SECRET_KEY in known_dev_secrets or len(self.SECRET_KEY) < 32:
                raise ValueError(
                    f"Production/Staging environment '{self.ENVIRONMENT}' requires a secure SECRET_KEY "
                    "with a minimum length of 32 characters, and cannot use development default secrets."
                )
        return self


settings = Settings()
