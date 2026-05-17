package forpdateam.ru.forpda.common.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import forpdateam.ru.forpda.model.interactors.events.EventsController
import forpdateam.ru.forpda.notifications.NotificationsService
import ru.radiationx.quill.getScope

/**
 * Created by isanechek on 7/11/17.
 */
class WakeUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SUKA", "RECIEVER ACTION " + intent.action)
        context.getScope().get(EventsController::class).start()
        NotificationsService.startAndCheck(context)
    }
}
