import pytest
from pydantic import ValidationError
from app.core.config import Settings


def test_settings_default_values() -> None:
    settings = Settings()
    assert settings.PROJECT_NAME == "NAGARLENS"
    assert settings.VERSION == "0.3.0"
    assert settings.ENVIRONMENT in ["development", "staging", "production"]
    assert "postgresql" in settings.DATABASE_URL
    assert isinstance(settings.CORS_ORIGINS, list)


def test_settings_cors_parser() -> None:
    settings = Settings(CORS_ORIGINS='["http://localhost:3000", "https://app.nagarlens.org"]')
    assert "http://localhost:3000" in settings.CORS_ORIGINS
    assert "https://app.nagarlens.org" in settings.CORS_ORIGINS


def test_development_default_secret_accepted() -> None:
    settings = Settings(ENVIRONMENT="development")
    assert settings.ENVIRONMENT == "development"


def test_production_default_secret_rejected() -> None:
    with pytest.raises(ValidationError) as exc_info:
        Settings(
            ENVIRONMENT="production",
            SECRET_KEY="dev_secret_key_minimum_32_chars_long_for_security_compliance",
        )
    assert "SECRET_KEY" in str(exc_info.value)


def test_production_short_secret_rejected() -> None:
    with pytest.raises(ValidationError) as exc_info:
        Settings(
            ENVIRONMENT="production",
            SECRET_KEY="short_secret_under_32_chars",
        )
    assert "SECRET_KEY" in str(exc_info.value)


def test_production_valid_secret_accepted() -> None:
    valid_prod_key = "a_highly_secure_production_secret_key_32_chars_long!"
    settings = Settings(
        ENVIRONMENT="production",
        SECRET_KEY=valid_prod_key,
    )
    assert settings.ENVIRONMENT == "production"
    assert settings.SECRET_KEY == valid_prod_key
