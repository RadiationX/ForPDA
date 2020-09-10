package forpdateam.ru.forpda.notifications;

import androidx.annotation.NonNull;

import com.evernote.android.job.Job;

public class NotificationsJob extends Job {

    public static final String TAG = "notifications_job_tag";

    @Override
    @NonNull
    protected Result onRunJob(final Params params) {
        boolean success = true;
        NotificationsService.startAndCheck();
        return success ? Result.SUCCESS : Result.FAILURE;
    }
}
