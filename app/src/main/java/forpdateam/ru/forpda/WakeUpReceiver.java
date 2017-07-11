package forpdateam.ru.forpda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Created by isanechek on 7/11/17.
 */

public class WakeUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NewWebSocketService.registerJob(context, 2); // for test interval 2 minute
        } else {
            context.startService(new Intent(context, WebSocketService.class));
        }
    }
}
