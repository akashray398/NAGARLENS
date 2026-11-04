# ADR-002: Hybrid Asset Identity Strategy

## Status
Accepted

## Context
NAGARLENS requires persistent asset identification across offline field devices, municipal databases, QR codes, and physical hardware tags. Using integer primary keys or raw QR strings as primary database identifiers leads to collisions and coupling issues.

## Decision
We adopt a **Hybrid Asset Identity** model:
1. **Canonical Primary Key:** Permanent UUIDv4 generated offline or online. Never changes.
2. **Operational Human Identifier:** `asset_code` formatted as `NAG-{TYPE_PREFIX}-{SEQ_NUM}` (e.g., `NAG-DRN-000104`). Unique per organization.
3. **Physical Tag Registry:** Physical QR codes and NFC tags are registered as revocable tokens linked to `asset_id` in `asset_identifier_registry`. Revoking a tag does not alter the asset UUID.

## Consequences
- Guarantees collision-free offline asset registration.
- Prevents database primary key leakage in physical QR codes.
- Enables physical tag replacement without breaking historical asset memory.
