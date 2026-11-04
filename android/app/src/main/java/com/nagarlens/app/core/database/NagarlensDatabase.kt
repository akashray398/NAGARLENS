package com.nagarlens.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [EvidenceEntity::class],
    version = 1,
    exportSchema = true
)
abstract class NagarlensDatabase : RoomDatabase() {
    abstract fun evidenceDao(): EvidenceDao
}
