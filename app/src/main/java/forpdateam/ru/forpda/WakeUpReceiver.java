package forpdateam.ru.forpda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import forpdateam.ru.forpda.notifications.NotificationsService;

/**
 * Created by isanechek on 7/11/17.
 */

public class WakeUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NewWebSocketService.registerJob(context, 20); // for test interval 2 minute
        } else {
            context.startService(new Intent(context, NotificationsService.class));
        }*/



        /*Sent when the user is present after
         * device wakes up (e.g when the keyguard is gone)
         * */
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {

        }
        /*Device is shutting down. This is broadcast when the device
         * is being shut down (completely turned off, not sleeping)
         * */
        else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {

        }

        Log.d("SUKA", "RECIEVER ACTION "+intent.getAction());

        NotificationsService.startAndCheck();
    }
}
