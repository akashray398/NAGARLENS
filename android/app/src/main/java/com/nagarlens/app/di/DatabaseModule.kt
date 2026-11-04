package com.nagarlens.app.di

import android.content.Context
import androidx.room.Room
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideEvidenceDao(
        database: NagarlensDatabase
    ): EvidenceDao {
        return database.evidenceDao()
    }
}
