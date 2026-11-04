package com.nagarlens.app.core.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EvidenceSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    open fun scheduleSync(evidenceId: String) {
        val workData = workDataOf(EvidenceSyncWorker.KEY_EVIDENCE_ID to evidenceId)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<EvidenceSyncWorker>()
            .setInputData(workData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_evidence_$evidenceId",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    open fun forceRetrySync(evidenceId: String) {
        val workData = workDataOf(EvidenceSyncWorker.KEY_EVIDENCE_ID to evidenceId)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<EvidenceSyncWorker>()
            .setInputData(workData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_evidence_$evidenceId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
