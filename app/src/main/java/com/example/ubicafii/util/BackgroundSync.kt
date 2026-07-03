package com.example.ubicafii.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun programarSincronizacion(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // Solo se ejecuta si hay internet
        .build()

    // El Worker se ejecutará cada hora
    val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "espacios_sync",
        ExistingPeriodicWorkPolicy.KEEP, // Si ya existe la tarea, la mantiene sin reiniciarla
        syncRequest
    )
}