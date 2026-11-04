package com.nagarlens.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [EvidenceEntity::class, AssetEntity::class],
    version = 2,
    exportSchema = true
)
abstract class NagarlensDatabase : RoomDatabase() {
    abstract fun evidenceDao(): EvidenceDao
    abstract fun assetDao(): AssetDao

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
    }
}
