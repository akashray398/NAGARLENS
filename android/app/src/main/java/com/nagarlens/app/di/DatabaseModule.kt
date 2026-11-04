package com.nagarlens.app.di

import android.content.Context
import androidx.room.Room
import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.EvidenceDao
import com.nagarlens.app.core.database.IncidentDao
import com.nagarlens.app.core.database.InterventionDao
import com.nagarlens.app.core.database.InterventionEvidenceLinkDao
import com.nagarlens.app.core.database.NagarlensDatabase
import com.nagarlens.app.core.database.NotificationDao
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
                NagarlensDatabase.MIGRATION_3_4,
                NagarlensDatabase.MIGRATION_4_5
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideEvidenceDao(
        database: NagarlensDatabase
    ): EvidenceDao = database.evidenceDao()

    @Provides
    @Singleton
    fun provideAssetDao(
        database: NagarlensDatabase
    ): AssetDao = database.assetDao()

    @Provides
    @Singleton
    fun provideObservationDao(
        database: NagarlensDatabase
    ): ObservationDao = database.observationDao()

    @Provides
    @Singleton
    fun provideObservationEvidenceLinkDao(
        database: NagarlensDatabase
    ): ObservationEvidenceLinkDao = database.observationEvidenceLinkDao()

    @Provides
    @Singleton
    fun provideInterventionDao(
        database: NagarlensDatabase
    ): InterventionDao = database.interventionDao()

    @Provides
    @Singleton
    fun provideInterventionEvidenceLinkDao(
        database: NagarlensDatabase
    ): InterventionEvidenceLinkDao = database.interventionEvidenceLinkDao()

    @Provides
    @Singleton
    fun provideOutcomeVerificationDao(
        database: NagarlensDatabase
    ): OutcomeVerificationDao = database.outcomeVerificationDao()

    @Provides
    @Singleton
    fun provideIncidentDao(
        database: NagarlensDatabase
    ): IncidentDao = database.incidentDao()

    @Provides
    @Singleton
    fun provideNotificationDao(
        database: NagarlensDatabase
    ): NotificationDao = database.notificationDao()
}
