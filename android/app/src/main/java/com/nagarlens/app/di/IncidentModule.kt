package com.nagarlens.app.di

import com.nagarlens.app.core.repository.IncidentRepository
import com.nagarlens.app.core.repository.IncidentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IncidentModule {

    @Binds
    @Singleton
    abstract fun bindIncidentRepository(
        impl: IncidentRepositoryImpl
    ): IncidentRepository
}
