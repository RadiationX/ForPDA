package forpdateam.ru.forpda.notifications

import com.evernote.android.job.Job

class NotificationsJob : Job() {
    override fun onRunJob(params: Params): Result {
        val success = true
        NotificationsService.startAndCheck()
        return if (success) Result.SUCCESS else Result.FAILURE
    }

    companion object {
        const val TAG: String = "notifications_job_tag"
    }
}
