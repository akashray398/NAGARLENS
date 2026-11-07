package com.nagarlens.app.di

import com.nagarlens.app.core.repository.SpatialRepository
import com.nagarlens.app.core.repository.SpatialRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpatialModule {

    @Binds
    @Singleton
    abstract fun bindSpatialRepository(
        impl: SpatialRepositoryImpl
    ): SpatialRepository
}
