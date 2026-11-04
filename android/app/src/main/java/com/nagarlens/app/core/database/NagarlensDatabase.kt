package com.nagarlens.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        EvidenceEntity::class,
        AssetEntity::class,
        ObservationEntity::class,
        ObservationEvidenceLinkEntity::class,
        InterventionEntity::class,
        InterventionEvidenceLinkEntity::class,
        InterventionStatusHistoryEntity::class,
        OutcomeVerificationEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class NagarlensDatabase : RoomDatabase() {
    abstract fun evidenceDao(): EvidenceDao
    abstract fun assetDao(): AssetDao
    abstract fun observationDao(): ObservationDao
    abstract fun observationEvidenceLinkDao(): ObservationEvidenceLinkDao
    abstract fun interventionDao(): InterventionDao
    abstract fun interventionEvidenceLinkDao(): InterventionEvidenceLinkDao
    abstract fun outcomeVerificationDao(): OutcomeVerificationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `assets_records` (
                        `id` TEXT NOT NULL,
                        `assetCode` TEXT NOT NULL,
                        `assetCategory` TEXT NOT NULL,
                        `lifecycleStatus` TEXT NOT NULL,
                        `verificationStatus` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `altitudeMeters` REAL,
                        `description` TEXT,
                        `version` INTEGER NOT NULL,
                        `createdBy` TEXT NOT NULL,
                        `createdTimestampUtc` INTEGER NOT NULL,
                        `updatedTimestampUtc` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `observations_records` (
                        `id` TEXT NOT NULL,
                        `assetId` TEXT NOT NULL,
                        `observationType` TEXT NOT NULL,
                        `conditionCategory` TEXT NOT NULL,
                        `conditionSeverity` TEXT NOT NULL,
                        `description` TEXT,
                        `observedAtUtc` INTEGER NOT NULL,
                        `recordedAtUtc` INTEGER NOT NULL,
                        `observedBy` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `locationAccuracyMeters` REAL,
                        `source` TEXT NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `interventions_records` (
                        `id` TEXT NOT NULL,
                        `assetId` TEXT NOT NULL,
                        `interventionType` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT,
                        `status` TEXT NOT NULL,
                        `plannedAtUtc` INTEGER,
                        `startedAtUtc` INTEGER,
                        `completedAtUtc` INTEGER,
                        `recordedBy` TEXT NOT NULL,
                        `createdAtUtc` INTEGER NOT NULL,
                        `updatedAtUtc` INTEGER NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `intervention_status_history_records` (
                        `id` TEXT NOT NULL,
                        `interventionId` TEXT NOT NULL,
                        `previousStatus` TEXT NOT NULL,
                        `newStatus` TEXT NOT NULL,
                        `reason` TEXT,
                        `actorId` TEXT NOT NULL,
                        `recordedAtUtc` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `outcome_verifications_records` (
                        `id` TEXT NOT NULL,
                        `interventionId` TEXT NOT NULL,
                        `assetId` TEXT NOT NULL,
                        `verificationObservationId` TEXT NOT NULL,
                        `outcome` TEXT NOT NULL,
                        `description` TEXT,
                        `verifiedAtUtc` INTEGER NOT NULL,
                        `verifiedBy` TEXT NOT NULL,
                        `createdAtUtc` INTEGER NOT NULL,
                        `syncStatus` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `observation_evidence_link_records` (
                        `id` TEXT NOT NULL,
                        `observationId` TEXT NOT NULL,
                        `evidenceId` TEXT NOT NULL,
                        `relationshipType` TEXT NOT NULL,
                        `linkedAtUtc` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `intervention_evidence_link_records` (
                        `id` TEXT NOT NULL,
                        `interventionId` TEXT NOT NULL,
                        `evidenceId` TEXT NOT NULL,
                        `relationshipType` TEXT NOT NULL,
                        `linkedAtUtc` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
