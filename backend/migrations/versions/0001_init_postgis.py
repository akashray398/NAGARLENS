"""0001_init_postgis

Revision ID: 0001_init_postgis
Revises:
Create Date: 2025-02-17 12:00:00.000000

"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa


revision: str = '0001_init_postgis'
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # Explicitly initialize required PostGIS & UUID extensions
    op.execute("CREATE EXTENSION IF NOT EXISTS postgis;")
    op.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";")


def downgrade() -> None:
    # Do not drop postgis extension on downgrade to preserve database stability
    pass
