package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.utils.GoogleDriveSyncManager

class AutoSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AutoSyncWorker", "Starting auto-sync to Google Drive...")
        val result = GoogleDriveSyncManager.syncToDrive(applicationContext)
        return if (result.isSuccess) {
            Log.d("AutoSyncWorker", "Auto-sync successful.")
            Result.success()
        } else {
            Log.e("AutoSyncWorker", "Auto-sync failed.", result.exceptionOrNull())
            Result.retry()
        }
    }
}
