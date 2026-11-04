"""NAGARLENS — Asset Domain Enums and Standards."""

from enum import Enum


class AssetCategory(str, Enum):
    STORM_WATER_DRAIN = "STORM_WATER_DRAIN"
    OPEN_DRAIN = "OPEN_DRAIN"
    COVERED_DRAIN = "COVERED_DRAIN"
    CATCH_BASIN = "CATCH_BASIN"
    MANHOLE = "MANHOLE"
    CULVERT = "CULVERT"
    DRAIN_OUTLET = "DRAIN_OUTLET"
    WATER_CHANNEL = "WATER_CHANNEL"
    OTHER = "OTHER"
    UNKNOWN = "UNKNOWN"

    @classmethod
    def get_prefix(cls, category: "AssetCategory") -> str:
        prefixes = {
            cls.STORM_WATER_DRAIN: "SWD",
            cls.OPEN_DRAIN: "OPD",
            cls.COVERED_DRAIN: "CVD",
            cls.CATCH_BASIN: "CB",
            cls.MANHOLE: "MH",
            cls.CULVERT: "CVT",
            cls.DRAIN_OUTLET: "OUT",
            cls.WATER_CHANNEL: "WCH",
            cls.OTHER: "OTH",
            cls.UNKNOWN: "UNK",
        }
        return prefixes.get(category, "AST")


class AssetLifecycleStatus(str, Enum):
    ACTIVE = "ACTIVE"
    UNDER_CONSTRUCTION = "UNDER_CONSTRUCTION"
    TEMPORARILY_CLOSED = "TEMPORARILY_CLOSED"
    OUT_OF_SERVICE = "OUT_OF_SERVICE"
    RETIRED = "RETIRED"


class AssetVerificationStatus(str, Enum):
    UNVERIFIED = "UNVERIFIED"
    PENDING_REVIEW = "PENDING_REVIEW"
    VERIFIED = "VERIFIED"
    REJECTED = "REJECTED"
    CORRECTION_REQUIRED = "CORRECTION_REQUIRED"


class IdentifierType(str, Enum):
    QR_CODE = "QR_CODE"
    NFC_TAG = "NFC_TAG"
    MUNICIPAL_ID = "MUNICIPAL_ID"


class AssetRelationshipType(str, Enum):
    INITIAL_REGISTRATION = "INITIAL_REGISTRATION"
    CONDITION_OBSERVATION = "CONDITION_OBSERVATION"
    DAMAGE_EVIDENCE = "DAMAGE_EVIDENCE"
    MAINTENANCE_EVIDENCE = "MAINTENANCE_EVIDENCE"
    LOCATION_VERIFICATION = "LOCATION_VERIFICATION"
    OTHER = "OTHER"


class VerificationDecision(str, Enum):
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"
    CORRECTION_REQUESTED = "CORRECTION_REQUESTED"
