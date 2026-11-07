package com.nagarlens.app.di

import com.nagarlens.app.core.repository.InvestigationRepository
import com.nagarlens.app.core.repository.InvestigationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InvestigationModule {

    @Binds
    @Singleton
    abstract fun bindInvestigationRepository(
        impl: InvestigationRepositoryImpl
    ): InvestigationRepository
}
