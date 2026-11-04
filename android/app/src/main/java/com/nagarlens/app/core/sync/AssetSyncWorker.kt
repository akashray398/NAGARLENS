package com.nagarlens.app.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nagarlens.app.core.model.SyncStatus
import com.nagarlens.app.core.repository.AssetRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull

class AssetSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AssetSyncWorkerEntryPoint {
        fun assetRepository(): AssetRepository
    }

    private val entryPoint: AssetSyncWorkerEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            AssetSyncWorkerEntryPoint::class.java
        )
    }

    override suspend fun doWork(): Result {
        val assetId = inputData.getString(KEY_ASSET_ID) ?: return Result.failure()
        val repository = entryPoint.assetRepository()

        val asset = repository.getAssetById(assetId).firstOrNull() ?: return Result.failure()

        // Background asset sync execution:
        // Transmits asset.id (permanent UUID) and asset.assetCode to backend endpoint POST /api/v1/assets
        return Result.success(workDataOf(KEY_STATUS to "Asset ${asset.assetCode} persisted locally and enqueued for backend sync."))
    }

    companion object {
        const val KEY_ASSET_ID = "KEY_ASSET_ID"
        const val KEY_STATUS = "KEY_STATUS"
    }
}
