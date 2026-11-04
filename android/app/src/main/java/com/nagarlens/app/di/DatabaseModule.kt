package com.nagarlens.app.di

import android.content.Context
import androidx.room.Room
import com.nagarlens.app.core.database.AssetDao
import com.nagarlens.app.core.database.EvidenceDao
import com.nagarlens.app.core.database.NagarlensDatabase
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
            .addMigrations(NagarlensDatabase.MIGRATION_1_2)
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
}
