package forpdateam.ru.forpda.notifications

import android.app.job.JobParameters
import android.app.job.JobService

/**
 * Created by radiationx on 07.11.17.
 */
class JobSchedulerService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }
}