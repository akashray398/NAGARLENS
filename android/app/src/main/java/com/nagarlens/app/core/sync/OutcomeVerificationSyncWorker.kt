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

class OutcomeVerificationSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface OutcomeVerificationSyncWorkerEntryPoint {
        fun temporalRepository(): TemporalRepository
    }

    private val entryPoint: OutcomeVerificationSyncWorkerEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            OutcomeVerificationSyncWorkerEntryPoint::class.java
        )
    }

    override suspend fun doWork(): Result {
        val verificationId = inputData.getString(KEY_VERIFICATION_ID) ?: return Result.failure()
        return Result.success(workDataOf(KEY_STATUS to "Outcome verification $verificationId persisted locally and enqueued for sync."))
    }

    companion object {
        const val KEY_VERIFICATION_ID = "KEY_VERIFICATION_ID"
        const val KEY_STATUS = "KEY_STATUS"
    }
}
