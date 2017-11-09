package forpdateam.ru.forpda.notifications;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

/**
 * Created by radiationx on 07.11.17.
 */

@TargetApi(21)
public class JobSchedulerService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}