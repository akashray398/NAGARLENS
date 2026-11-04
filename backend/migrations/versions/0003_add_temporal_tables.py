"""0003_add_temporal_tables

Revision ID: 0003_add_temporal_tables
Revises: 0002_add_asset_tables
Create Date: 2025-02-18 14:00:00.000000

"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa


revision: str = '0003_add_temporal_tables'
down_revision: Union[str, None] = '0002_add_asset_tables'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # 1. Observations Table
    op.create_table(
        'observations',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('asset_id', sa.String(length=36), nullable=False),
        sa.Column('observation_type', sa.String(length=32), nullable=False),
        sa.Column('condition_category', sa.String(length=32), nullable=False),
        sa.Column('condition_severity', sa.String(length=32), nullable=False, server_default='UNKNOWN'),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('observed_at_utc', sa.BigInteger(), nullable=False),
        sa.Column('recorded_at_utc', sa.BigInteger(), nullable=False),
        sa.Column('observed_by', sa.String(length=64), nullable=False),
        sa.Column('latitude', sa.Double(), nullable=False),
        sa.Column('longitude', sa.Double(), nullable=False),
        sa.Column('location_accuracy_meters', sa.Float(), nullable=True),
        sa.Column('source', sa.String(length=32), nullable=False, server_default='FIELD_INSPECTION'),
        sa.ForeignKeyConstraint(['asset_id'], ['assets.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_observations_asset', 'observations', ['asset_id'], unique=False)
    op.create_index('idx_observations_time', 'observations', ['observed_at_utc'], unique=False)

    # 2. Observation Evidence Links Table
    op.create_table(
        'observation_evidence_links',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('observation_id', sa.String(length=36), nullable=False),
        sa.Column('evidence_id', sa.String(length=64), nullable=False),
        sa.Column('relationship_type', sa.String(length=32), nullable=False, server_default='CONDITION_PROOF'),
        sa.Column('linked_at_utc', sa.BigInteger(), nullable=False),
        sa.ForeignKeyConstraint(['observation_id'], ['observations.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_obs_evidence_obs', 'observation_evidence_links', ['observation_id'], unique=False)

    # 3. Interventions Table
    op.create_table(
        'interventions',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('asset_id', sa.String(length=36), nullable=False),
        sa.Column('intervention_type', sa.String(length=32), nullable=False),
        sa.Column('title', sa.String(length=128), nullable=False),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('status', sa.String(length=32), nullable=False, server_default='PROPOSED'),
        sa.Column('planned_at_utc', sa.BigInteger(), nullable=True),
        sa.Column('started_at_utc', sa.BigInteger(), nullable=True),
        sa.Column('completed_at_utc', sa.BigInteger(), nullable=True),
        sa.Column('recorded_by', sa.String(length=64), nullable=False),
        sa.Column('created_at_utc', sa.BigInteger(), nullable=False),
        sa.Column('updated_at_utc', sa.BigInteger(), nullable=False),
        sa.ForeignKeyConstraint(['asset_id'], ['assets.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_interventions_asset', 'interventions', ['asset_id'], unique=False)

    # 4. Intervention Evidence Links Table
    op.create_table(
        'intervention_evidence_links',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('intervention_id', sa.String(length=36), nullable=False),
        sa.Column('evidence_id', sa.String(length=64), nullable=False),
        sa.Column('relationship_type', sa.String(length=32), nullable=False, server_default='BEFORE_ACTION'),
        sa.Column('linked_at_utc', sa.BigInteger(), nullable=False),
        sa.ForeignKeyConstraint(['intervention_id'], ['interventions.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_int_evidence_int', 'intervention_evidence_links', ['intervention_id'], unique=False)

    # 5. Intervention Status History Table
    op.create_table(
        'intervention_status_history',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('intervention_id', sa.String(length=36), nullable=False),
        sa.Column('previous_status', sa.String(length=32), nullable=False),
        sa.Column('new_status', sa.String(length=32), nullable=False),
        sa.Column('reason', sa.Text(), nullable=True),
        sa.Column('actor_id', sa.String(length=64), nullable=False),
        sa.Column('recorded_at_utc', sa.BigInteger(), nullable=False),
        sa.ForeignKeyConstraint(['intervention_id'], ['interventions.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_int_status_hist_int', 'intervention_status_history', ['intervention_id'], unique=False)

    # 6. Outcome Verifications Table
    op.create_table(
        'outcome_verifications',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('intervention_id', sa.String(length=36), nullable=False),
        sa.Column('asset_id', sa.String(length=36), nullable=False),
        sa.Column('verification_observation_id', sa.String(length=36), nullable=False),
        sa.Column('outcome', sa.String(length=32), nullable=False),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('verified_at_utc', sa.BigInteger(), nullable=False),
        sa.Column('verified_by', sa.String(length=64), nullable=False),
        sa.Column('created_at_utc', sa.BigInteger(), nullable=False),
        sa.ForeignKeyConstraint(['intervention_id'], ['interventions.id'], ondelete='CASCADE'),
        sa.ForeignKeyConstraint(['asset_id'], ['assets.id'], ondelete='CASCADE'),
        sa.ForeignKeyConstraint(['verification_observation_id'], ['observations.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('idx_verifications_intervention', 'outcome_verifications', ['intervention_id'], unique=False)
    op.create_index('idx_verifications_asset', 'outcome_verifications', ['asset_id'], unique=False)


def downgrade() -> None:
    op.drop_table('outcome_verifications')
    op.drop_table('intervention_status_history')
    op.drop_table('intervention_evidence_links')
    op.drop_table('interventions')
    op.drop_table('observation_evidence_links')
    op.drop_table('observations')
