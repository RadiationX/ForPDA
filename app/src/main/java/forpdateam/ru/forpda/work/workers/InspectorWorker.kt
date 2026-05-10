package forpdateam.ru.forpda.work.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import forpdateam.ru.forpda.App

class InspectorWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val eventsController = App.get().Di().eventsController
        eventsController.start()
        eventsController.checkEvents()
        return Result.success()
    }
}