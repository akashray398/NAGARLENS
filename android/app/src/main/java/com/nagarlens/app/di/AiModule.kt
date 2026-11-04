package com.nagarlens.app.di

import com.nagarlens.app.core.ml.DefectAnalyzer
import com.nagarlens.app.core.ml.UnavailableDefectAnalyzer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindDefectAnalyzer(
        impl: UnavailableDefectAnalyzer
    ): DefectAnalyzer
}
