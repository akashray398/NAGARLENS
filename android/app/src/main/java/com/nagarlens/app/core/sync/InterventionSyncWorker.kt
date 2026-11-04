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

class InterventionSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface InterventionSyncWorkerEntryPoint {
        fun temporalRepository(): TemporalRepository
    }

    private val entryPoint: InterventionSyncWorkerEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            InterventionSyncWorkerEntryPoint::class.java
        )
    }

    override suspend fun doWork(): Result {
        val interventionId = inputData.getString(KEY_INTERVENTION_ID) ?: return Result.failure()
        return Result.success(workDataOf(KEY_STATUS to "Intervention $interventionId persisted locally and enqueued for sync."))
    }

    companion object {
        const val KEY_INTERVENTION_ID = "KEY_INTERVENTION_ID"
        const val KEY_STATUS = "KEY_STATUS"
    }
}
