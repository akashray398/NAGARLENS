package com.nagarlens.app.di

import com.nagarlens.app.core.repository.PredictiveRepository
import com.nagarlens.app.core.repository.PredictiveRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PredictiveModule {

    @Binds
    @Singleton
    abstract fun bindPredictiveRepository(
        impl: PredictiveRepositoryImpl
    ): PredictiveRepository
}
