"""0002_add_asset_tables

Revision ID: 0002_add_asset_tables
Revises: 0001_init_postgis
Create Date: 2025-02-18 10:00:00.000000

"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa
from geoalchemy2 import Geometry


revision: str = '0002_add_asset_tables'
down_revision: Union[str, None] = '0001_init_postgis'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # 1. Assets Table
    op.create_table(
        'assets',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('asset_code', sa.String(length=32), nullable=False),
        sa.Column('asset_category', sa.String(length=32), nullable=False),
        sa.Column('lifecycle_status', sa.String(length=32), nullable=False, server_default='ACTIVE'),
        sa.Column('verification_status', sa.String(length=32), nullable=False, server_default='UNVERIFIED'),
        sa.Column('location', Geometry(geometry_type='POINT', srid=4326, spatial_index=True), nullable=True),
        sa.Column('latitude', sa.Double(), nullable=False),
        sa.Column('longitude', sa.Double(), nullable=False),
        sa.Column('altitude_meters', sa.Float(), nullable=True),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('version', sa.Integer(), nullable=False, server_default='1'),
        sa.Column('created_by', sa.String(length=64), nullable=False, server_default='system'),
        sa.Column('created_timestamp_utc', sa.BigInteger(), nullable=False),
        sa.Column('updated_timestamp_utc', sa.BigInteger(), nullable=False),
        sa.PrimaryKeyConstraint('id'),
        sa.UniqueConstraint('asset_code', name='uq_assets_asset_code')
    )
    op.create_index('idx_assets_category', 'assets', ['asset_category'], unique=False)
    op.create_index('idx_assets_verification', 'assets', ['verification_status'], unique=False)

    # 2. Asset Location History Table
    op.create_table(
        'asset_location_history',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('asset_id', sa.String(length=36), nullable=False),
        sa.Column('previous_latitude', sa.Double(), nullable=False),
        sa.Column('previous_longitude', sa.Double(), nullable=False),
        sa.Column('new_latitude', sa.Double(), nullable=False),
        sa.Column('new_longitude', sa.Double(), nullable=False),
        sa.Column('reason', sa.Text(), nullable=True),
        sa.Column('actor_id', sa.String(length=64), nullable=False),
        sa.Column('recorded_timestamp_utc', sa.BigInteger(), nullable=False),
        sa.ForeignKeyConstraint(['asset_id'], ['assets.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_location_history_asset', 'asset_location_history', ['asset_id'], unique=False)

    # 3. Asset Identifier Registry Table
    op.create_table(
        'asset_identifier_registry',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('asset_id', sa.String(length=36), nullable=False),
        sa.Column('identifier_type', sa.String(length=32), nullable=False),
        sa.Column('identifier_value', sa.String(length=128), nullable=False),
        sa.Column('is_active', sa.Boolean(), nullable=False, server_default='true'),
        sa.Column('created_timestamp_utc', sa.BigInteger(), nullable=False),
        sa.Column('revoked_timestamp_utc', sa.BigInteger(), nullable=True),
        sa.ForeignKeyConstraint(['asset_id'], ['assets.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_identifier_value', 'asset_identifier_registry', ['identifier_value'], unique=False)

    # 4. Asset Evidence Links Table
    op.create_table(
        'asset_evidence_links',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('asset_id', sa.String(length=36), nullable=False),
        sa.Column('evidence_id', sa.String(length=64), nullable=False),
        sa.Column('relationship_type', sa.String(length=32), nullable=False, server_default='CONDITION_OBSERVATION'),
        sa.Column('created_by', sa.String(length=64), nullable=False, server_default='system'),
        sa.Column('created_timestamp_utc', sa.BigInteger(), nullable=False),
        sa.ForeignKeyConstraint(['asset_id'], ['assets.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_evidence_links_asset', 'asset_evidence_links', ['asset_id'], unique=False)
    op.create_index('idx_evidence_links_evidence', 'asset_evidence_links', ['evidence_id'], unique=False)

    # 5. Asset Verification Records Table
    op.create_table(
        'asset_verification_records',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('asset_id', sa.String(length=36), nullable=False),
        sa.Column('decision', sa.String(length=32), nullable=False),
        sa.Column('reason', sa.Text(), nullable=True),
        sa.Column('reviewer_id', sa.String(length=64), nullable=False),
        sa.Column('decision_timestamp_utc', sa.BigInteger(), nullable=False),
        sa.ForeignKeyConstraint(['asset_id'], ['assets.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_verification_records_asset', 'asset_verification_records', ['asset_id'], unique=False)


def downgrade() -> None:
    op.drop_table('asset_verification_records')
    op.drop_table('asset_evidence_links')
    op.drop_table('asset_identifier_registry')
    op.drop_table('asset_location_history')
    op.drop_table('assets')
