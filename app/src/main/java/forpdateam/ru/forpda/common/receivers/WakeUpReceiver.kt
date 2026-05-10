package forpdateam.ru.forpda.common.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.notifications.NotificationsService

/**
 * Created by isanechek on 7/11/17.
 */
class WakeUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SUKA", "RECIEVER ACTION " + intent.action)
        App.get().Di().eventsController.start()
        NotificationsService.startAndCheck()
    }
}
