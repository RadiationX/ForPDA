package forpdateam.ru.forpda.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import forpdateam.ru.forpda.work.workers.InspectorWorker
import java.util.concurrent.TimeUnit

object WorkUtils {

    fun enqueuePeriodicInspectorCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<InspectorWorker>(
            repeatInterval = 15L,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        ).setConstraints(constraints).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PeriodicInspector",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}