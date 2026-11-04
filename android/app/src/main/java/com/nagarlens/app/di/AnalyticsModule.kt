package com.nagarlens.app.di

import com.nagarlens.app.core.repository.AnalyticsRepository
import com.nagarlens.app.core.repository.AnalyticsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: AnalyticsRepositoryImpl
    ): AnalyticsRepository
}
