# ADR-003: Relational Temporal Reality Graph Architecture

## Status
Accepted

## Context
NAGARLENS requires a persistent, evidence-backed timeline tracking asset condition changes, maintenance interventions, and verification outcomes over time. Introducing a separate graph database (e.g. Neo4j) creates unnecessary operational overhead and transactional sync complexity.

## Decision
We implement a **Relational Temporal Reality Graph** within PostgreSQL 16 + PostGIS / Room SQLite:
1. `observations` table tracks time-series inspection facts (`observed_at_utc`).
2. `interventions` table tracks maintenance actions (`DRAIN_CLEANING`, `SILT_REMOVAL`).
3. `outcome_verifications` table connects interventions to verification observations, recording empirical outcomes (`IMPROVED`, `DETERIORATED`).
4. All temporal entries are append-only.

## Consequences
- Relational foreign keys and indexes guarantee strict data integrity and fast chronological queries.
- Zero microservice / graph database operational overhead.
- Enables seamless offline Room database synchronization across field devices.
