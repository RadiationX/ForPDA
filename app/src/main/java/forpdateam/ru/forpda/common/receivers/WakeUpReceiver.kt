package forpdateam.ru.forpda.common.receivers;

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
        Log.d("SUKA", "RECIEVER ACTION " + intent.getAction());
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                NotificationsService.startAndCheck();
            } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                NotificationsService.startAndCheck();
            }
        }
    }
}
