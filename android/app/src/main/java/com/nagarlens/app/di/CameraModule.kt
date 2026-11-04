package com.nagarlens.app.di

import android.content.Context
import com.nagarlens.app.core.camera.CameraXController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {

    @Provides
    @Singleton
    fun provideCameraXController(
        @ApplicationContext context: Context
    ): CameraXController = CameraXController(context)
}
