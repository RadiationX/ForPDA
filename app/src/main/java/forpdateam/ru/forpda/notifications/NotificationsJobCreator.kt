package forpdateam.ru.forpda.notifications

import android.content.Context
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.evernote.android.job.JobCreator.AddJobCreatorReceiver
import com.evernote.android.job.JobManager

class NotificationsJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        return when (tag) {
            NotificationsJob.TAG -> NotificationsJob()
            else -> null
        }
    }

    class AddReceiver : AddJobCreatorReceiver() {
        override fun addJobCreator(context: Context, manager: JobManager) {
            // manager.addJobCreator(new NotificationsJobCreator());
        }
    }
}
