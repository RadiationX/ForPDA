package forpdateam.ru.forpda.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.App.Companion.getContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by radiationx on 31.07.17.
 */
class NotificationsService : Service() {
    private var lastHardCheckTime: Long = 0

    private val eventsRepository = get().Di().eventsController
    private val notificationEventSender = get().Di().notificationEventSender

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.i(LOG_TAG, "onCreate")

        eventsRepository
            .observeNewEvents()
            .onEach { notificationEventSender.send(this, it) }
            .launchIn(coroutineScope)

        eventsRepository
            .observeCancelIds()
            .onEach { notificationEventSender.cancel(this, it) }
            .launchIn(coroutineScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(LOG_TAG, "onStartCommand args $flags : $startId : $intent")

        val time = SystemClock.elapsedRealtime()

        Log.d(LOG_TAG, "Handle check last events: $time : $lastHardCheckTime : ${time - lastHardCheckTime}")

        if ((time - lastHardCheckTime) >= 1000 * 60) {
            lastHardCheckTime = time
            coroutineScope.launch {
                eventsRepository.checkEvents()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(LOG_TAG, "onDestroy")
        coroutineScope.cancel()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        Log.i(LOG_TAG, "onTaskRemoved")
    }

    companion object {
        private val LOG_TAG = NotificationsService::class.java.simpleName
        fun startAndCheck() {
            try {
                val intent = Intent(getContext(), NotificationsService::class.java)
                getContext().startService(intent)
            } catch (ex: Exception) {
                Log.e(LOG_TAG, "startAndCheck", ex)
            }
        }
    }
}
