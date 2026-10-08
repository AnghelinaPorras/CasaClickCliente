package com.casaclick.cliente.worker
import android.content.Context
import androidx.work.*
import com.casaclick.cliente.CasaClickApplication
import java.util.concurrent.TimeUnit
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context,params) {
    override suspend fun doWork(): Result {
        val container=(applicationContext as CasaClickApplication).container
        return if(container.sync.sincronizar()) Result.success() else Result.retry()
    }
    companion object {
        fun programar(context: Context) {
            val constraints=Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val work=OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).build()
            WorkManager.getInstance(context).enqueueUniqueWork("sincronizacion_inmediata",ExistingWorkPolicy.APPEND_OR_REPLACE,work)
        }
        fun periodico(context: Context) {
            val work=PeriodicWorkRequestBuilder<SyncWorker>(15,TimeUnit.MINUTES).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("sincronizacion_periodica",ExistingPeriodicWorkPolicy.KEEP,work)
        }
    }
}
