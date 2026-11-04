package com.nagarlens.app.di

import com.nagarlens.app.core.repository.ReportRepository
import com.nagarlens.app.core.repository.ReportRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReportModule {

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        impl: ReportRepositoryImpl
    ): ReportRepository
}
