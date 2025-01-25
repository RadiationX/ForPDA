package forpdateam.ru.forpda.common.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import forpdateam.ru.forpda.notifications.NotificationsService

/**
 * Created by isanechek on 7/11/17.
 */
class WakeUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SUKA", "RECIEVER ACTION " + intent.action)
        val action = intent.action
        if (action != null) {
            if (action == Intent.ACTION_SCREEN_ON) {
                NotificationsService.startAndCheck()
            } else if (action == Intent.ACTION_BOOT_COMPLETED) {
                NotificationsService.startAndCheck()
            }
        }
    }
}
