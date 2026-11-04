package com.nagarlens.app.di

import com.nagarlens.app.core.repository.TemporalRepository
import com.nagarlens.app.core.repository.TemporalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TemporalModule {

    @Binds
    @Singleton
    abstract fun bindTemporalRepository(
        impl: TemporalRepositoryImpl
    ): TemporalRepository
}
