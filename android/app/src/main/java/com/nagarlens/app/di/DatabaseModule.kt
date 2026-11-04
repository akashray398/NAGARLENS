package com.nagarlens.app.di

import android.content.Context
import androidx.room.Room
import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.EvidenceDao
import com.nagarlens.app.core.database.InterventionDao
import com.nagarlens.app.core.database.InterventionEvidenceLinkDao
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.database.ObservationDao
import com.nagarlens.app.core.database.ObservationEvidenceLinkDao
import com.nagarlens.app.core.database.OutcomeVerificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNagarlensDatabase(
        @ApplicationContext context: Context
    ): NagarlensDatabase {
        return Room.databaseBuilder(
            context,
            NagarlensDatabase::class.java,
            "nagarlens_evidence.db"
        )
            .addMigrations(
                NagarlensDatabase.MIGRATION_1_2,
                NagarlensDatabase.MIGRATION_2_3,
                NagarlensDatabase.MIGRATION_3_4
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideEvidenceDao(
        database: NagarlensDatabase
    ): EvidenceDao {
        return database.evidenceDao()
    }

    @Provides
    @Singleton
    fun provideAssetDao(
        database: NagarlensDatabase
    ): AssetDao {
        return database.assetDao()
    }

    @Provides
    @Singleton
    fun provideObservationDao(
        database: NagarlensDatabase
    ): ObservationDao {
        return database.observationDao()
    }

    @Provides
    @Singleton
    fun provideObservationEvidenceLinkDao(
        database: NagarlensDatabase
    ): ObservationEvidenceLinkDao {
        return database.observationEvidenceLinkDao()
    }

    @Provides
    @Singleton
    fun provideInterventionDao(
        database: NagarlensDatabase
    ): InterventionDao {
        return database.interventionDao()
    }

    @Provides
    @Singleton
    fun provideInterventionEvidenceLinkDao(
        database: NagarlensDatabase
    ): InterventionEvidenceLinkDao {
        return database.interventionEvidenceLinkDao()
    }

    @Provides
    @Singleton
    fun provideOutcomeVerificationDao(
        database: NagarlensDatabase
    ): OutcomeVerificationDao {
        return database.outcomeVerificationDao()
    }
}
