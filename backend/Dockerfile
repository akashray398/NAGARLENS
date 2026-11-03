FROM python:3.12-slim

WORKDIR /app

# Prevent Python from writing bytecode and buffer outputs
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

# Install system dependencies required for psycopg2 / PostGIS client libraries
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libpq-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create dedicated non-root application user with UID 1000
RUN groupadd -g 1000 appuser && useradd -u 1000 -g appuser -m -s /bin/bash appuser

# Copy project specification & install dependencies
COPY pyproject.toml .
COPY README.md .
RUN pip install --no-cache-dir .[dev]

# Copy application source code & migrations
COPY app/ app/
COPY migrations/ migrations/
COPY alembic.ini .

# Set appropriate ownership for application files
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
