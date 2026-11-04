package com.nagarlens.app.di

import com.nagarlens.app.core.integrity.EvidenceFileStore
import com.nagarlens.app.core.integrity.Sha256Hasher
import com.nagarlens.app.core.repository.EvidenceRepository
import com.nagarlens.app.core.repository.EvidenceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EvidenceModule {

    @Binds
    @Singleton
    abstract fun bindEvidenceRepository(
        impl: EvidenceRepositoryImpl
    ): EvidenceRepository
}
