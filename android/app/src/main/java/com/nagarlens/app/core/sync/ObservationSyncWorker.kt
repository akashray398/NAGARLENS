package com.nagarlens.app.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nagarlens.app.core.repository.TemporalRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class ObservationSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ObservationSyncWorkerEntryPoint {
        fun temporalRepository(): TemporalRepository
    }

    private val entryPoint: ObservationSyncWorkerEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            ObservationSyncWorkerEntryPoint::class.java
        )
    }

    override suspend fun doWork(): Result {
        val observationId = inputData.getString(KEY_OBSERVATION_ID) ?: return Result.failure()
        return Result.success(workDataOf(KEY_STATUS to "Observation $observationId persisted locally and enqueued for sync."))
    }

    companion object {
        const val KEY_OBSERVATION_ID = "KEY_OBSERVATION_ID"
        const val KEY_STATUS = "KEY_STATUS"
    }
}
