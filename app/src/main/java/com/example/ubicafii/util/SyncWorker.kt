package com.example.ubicafii.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ubicafii.data.repository.EspacioRepository

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val repo = EspacioRepository(applicationContext)
            repo.refrescarEspacios()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}