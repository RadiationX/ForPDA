package forpdateam.ru.forpda.model.repository.events

import android.content.Context
import androidx.collection.ArraySet
import android.util.Log
import com.jakewharton.rxrelay2.PublishRelay
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.client.WebSocketController
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.common.AuthState
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.NetworkStateProvider
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.events.NotificationEventsApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class EventsRepository(
        private val context: Context,
        private val webClient: IWebClient,
        private val eventsApi: NotificationEventsApi,
        private val schedulers: SchedulersProvider,
        private val networkStateProvider: NetworkStateProvider,
        private val authHolder: AuthHolder,
        private val notificationPreferencesHolder: NotificationPreferencesHolder
) : BaseRepository(schedulers) {
    companion object {
        private const val LOG_TAG = "EventsRepository"
        private const val STACKED_MAX = 4
    }

    private var timerPeriod = (10 * 1000).toLong()

    private val pendingEvents = mapOf<NotificationEvent.Source, MutableMap<Int, NotificationEvent>>(
            NotificationEvent.Source.QMS to mutableMapOf(),
            NotificationEvent.Source.THEME to mutableMapOf(),
            NotificationEvent.Source.SITE to mutableMapOf()
    )

    private var checkTimer: Timer? = null
    private val timerRunnable = {
        for (source in pendingEvents.keys) {
            handlePendingEvents(source)
        }
    }

    private var lastNetworkState: Boolean = networkStateProvider.getState()
    private var lastAuthState: Boolean = authHolder.get().isAuth()
    private val eventsHistory = mutableMapOf<Int, NotificationEvent>()


    private val notifyRelay = PublishRelay.create<NotificationEvent>()
    private val notifyStackRelay = PublishRelay.create<List<NotificationEvent>>()
    private val cancelRelay = PublishRelay.create<NotificationEvent>()
    private val notifyTabRelay = PublishRelay.create<TabNotification>()

    private val controllerListener: WebSocketController.Listener = object : WebSocketController.Listener() {
        override fun onConnected() {
            Log.d(LOG_TAG, "WSContr onConnected ${webSocketController.getCurrentId()},  ${webSocketController.isConnected()}")
            webSocketController.send("""[${webSocketController.getCurrentId()}, "sv"]""")
            webSocketController.send("""[0, "ea", "u${authHolder.get().userId}"]""")
        }

        override fun onMessage(text: String?) {
            Log.d(LOG_TAG, "WSContr onMessage ${webSocketController.getCurrentId()}, ${webSocketController.isConnected()}, $text")
            try {
                eventsApi.parseWebSocketEvent(text)?.also {
                    if (it.type != NotificationEvent.Type.HAT_EDITED) {
                        handleWebSocketEvent(it)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        override fun onDisconnected(throwable: Throwable, response: Response?) {
            Log.d(LOG_TAG, "WSContr onDisconnected ${webSocketController.getCurrentId()}, ${webSocketController.isConnected()}, ${throwable.message}, $response")
            if (response != null) {
                Log.d(LOG_TAG, "WSContr onDisconnected: code=${response.code()}")
                if (response.code() == 403) {
                    App.get().notifyForbidden(true)
                }
            }

            throwable.printStackTrace()
            if (throwable is SocketTimeoutException || throwable is TimeoutException) {
                Log.d(LOG_TAG, "start onFailure")
                start(true)
            }
        }
    }

    private val webSocketController = WebSocketController(webClient, controllerListener)

    init {
        val networkDisposable = networkStateProvider
                .observeState()
                .filter { lastNetworkState != it }
                .subscribe {
                    lastNetworkState = it
                    if (it) {
                        Log.d(LOG_TAG, "start networkStateProvider.observeState")
                        start(true)
                    }
                }

        val authDisposable = authHolder
                .observe()
                .filter { it.isAuth() != lastAuthState }
                .subscribe {
                    Log.e("kulolo", "events rep observe authHolder ${it.state}")
                    lastAuthState = it.isAuth()
                    if (it.isAuth()) {
                        if (webSocketController.isConnected()) {
                            stop()
                        }
                        Log.d(LOG_TAG, "start authHolder.observe")
                        start(true)
                    } else {
                        stop()
                    }
                }

        var lastTimerStamp = System.currentTimeMillis()

        val timerDisposable = Observable
                .interval(1, TimeUnit.MINUTES)
                //.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe {
                    Log.d(LOG_TAG, "start timer $it (${(System.currentTimeMillis() - lastTimerStamp) / 1000}), ${webSocketController.isConnected()}")
                    lastTimerStamp = System.currentTimeMillis()
                    if (!webSocketController.isConnected()) {
                        stop()
                        start(false)
                    }
                }

        timerPeriod = notificationPreferencesHolder.getMainLimit()
    }

    fun observeEvents(): Observable<NotificationEvent> = notifyRelay
            .observeOn(schedulers.ui())

    fun observeEventsStack(): Observable<List<NotificationEvent>> = notifyStackRelay
            .observeOn(schedulers.ui())

    fun observeCancel(): Observable<NotificationEvent> = cancelRelay
            .observeOn(schedulers.ui())

    fun observeEventsTab(): Observable<TabNotification> = notifyTabRelay
            .observeOn(schedulers.ui())

    fun setTimerPeriod(period: Long) {
        timerPeriod = period
        resetTimer()
    }

    fun externalStart(checkEvents: Boolean) {
        Log.e(LOG_TAG, "start externalStart")
        start(checkEvents)
    }

    fun updateEvents(source: NotificationEvent.Source) {
        hardHandleEvent(source)
    }

    private fun start(checkEvents: Boolean) {
        Log.e(LOG_TAG, "Start: ${networkStateProvider.getState()} : ${webSocketController.isConnected()} : $checkEvents : ${webSocketController.getCurrentId()}")
        if (networkStateProvider.getState() && authHolder.get().isAuth()) {
            if (!webSocketController.isConnected()) {
                webSocketController.connect()
            }

            if (checkEvents) {
                hardHandleEvent(NotificationEvent.Source.THEME)
                hardHandleEvent(NotificationEvent.Source.QMS)
            }
            Log.d("SUKA", "PERIOD BLYAD $timerPeriod")
            resetTimer()
        }
    }

    private fun stop() {
        Log.d(LOG_TAG, "stop")
        cancelTimer()
        webSocketController.disconnectAll()
    }

    private fun resetTimer() {
        cancelTimer()
        checkTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    timerRunnable.invoke()
                }
            }, 0, timerPeriod)
        }
    }

    private fun cancelTimer() {
        checkTimer?.apply {
            cancel()
            purge()
        }
        checkTimer = null
    }

    private fun sendNotification(event: NotificationEvent) {
        Log.e("events_lalala", "send notification rep " + event.sourceEventText + " : " + event.source + " : " + event.sourceTitle + " : " + event.userNick)
        if (event.userId == authHolder.get().userId) {
            return
        }
        eventsHistory[event.notifyId()] = event
        if (!checkNotify(event, event.source)) {
            return
        }
        notifyRelay.accept(event)
    }

    private fun sendNotifications(events: List<NotificationEvent>, tSource: NotificationEvent.Source) {
        if (events.isEmpty()) {
            return
        }
        if (events.size <= STACKED_MAX) {
            for (event in events) {
                sendNotification(event)
            }
            return
        }
        if (!checkNotify(null, tSource)) {
            return
        }
        notifyStackRelay.accept(events)
    }

    private fun notifyTabs(event: TabNotification) {
        Log.d("SUKA", "notifyTabs")
        notifyTabRelay.accept(event)
    }

    private fun checkNotify(event: NotificationEvent?, source: NotificationEvent.Source): Boolean {
        if (!notificationPreferencesHolder.getMainEnabled()) {
            return false
        }
        if (NotificationEvent.fromQms(source)) {
            if (!notificationPreferencesHolder.getQmsEnabled()) {
                return false
            }
        } else if (NotificationEvent.fromTheme(source)) {
            if (event != null && event.isMention) {
                if (!notificationPreferencesHolder.getMentionsEnabled()) {
                    return false
                }
            } else {
                if (!notificationPreferencesHolder.getFavEnabled()) {
                    return false
                }
            }
        }
        return true
    }

    private fun checkOldEvent(event: NotificationEvent) {
        var oldEvent = eventsHistory[event.notifyId(NotificationEvent.Type.NEW)]
        var delete = false

        Log.e("kulolo", "checkOldEvent \n${oldEvent} \n$event")

        if (event.fromTheme()) {
            //Убираем уведомления избранного
            if (oldEvent != null && event.messageId >= oldEvent.messageId) {
                cancelRelay.accept(oldEvent)
                delete = true
            }

            //Убираем уведомление упоминаний
            oldEvent = eventsHistory[event.notifyId(NotificationEvent.Type.MENTION)]
            if (oldEvent != null) {
                cancelRelay.accept(oldEvent)
                delete = true
            }
        } else if (event.fromQms()) {

            //Убираем уведомление кумыса
            if (oldEvent != null) {
                cancelRelay.accept(oldEvent)
                delete = true
            }
        }

        if (delete || oldEvent == null) {
            notifyTabs(TabNotification(
                    event.source,
                    event.type,
                    event,
                    true
            ))
        }
        if (delete) {
            eventsHistory.remove(event.notifyId(NotificationEvent.Type.NEW))
        }
    }

    private fun checkOldEvents(loadedEvents: List<NotificationEvent>, source: NotificationEvent.Source) {
        val oldEvents = eventsHistory.filter { it.value.source == source }.map { it.value }

        for (oldEvent in oldEvents) {
            var exist = false
            for (loadedEvent in loadedEvents) {
                if (oldEvent.sourceId == loadedEvent.sourceId) {
                    exist = true
                    break
                }
            }
            if (!exist) {
                cancelRelay.accept(oldEvent)
                eventsHistory.remove(oldEvent.notifyId(NotificationEvent.Type.NEW))
                notifyTabs(TabNotification(
                        oldEvent.source,
                        NotificationEvent.Type.READ,
                        oldEvent,
                        true
                ))
            }
        }
    }

    private fun handleWebSocketEvent(event: NotificationEvent) {
        if (event.isRead) {
            checkOldEvent(event)
            return
        }
        eventsHistory[event.notifyId()] = event
        notifyTabs(TabNotification(
                event.source,
                event.type,
                event,
                true
        ))
        handleEvent(listOf(event), event.source)
    }


    private fun handleEvent(events: List<NotificationEvent>, source: NotificationEvent.Source) {
        val pending = pendingEvents[source]
        if (pending != null) {
            for (event in events) {
                pending[event.sourceId] = event
            }
        }
    }

    private fun hardHandleEvent(source: NotificationEvent.Source) {
        hardHandleEvent(emptyList(), source)
    }

    private fun hardHandleEvent(events: List<NotificationEvent>, source: NotificationEvent.Source) {
        Log.d("SUKA", "hardHandleEvent " + events.size + " : " + source)
        if (NotificationEvent.fromSite(source)) {
            if (notificationPreferencesHolder.getMentionsEnabled()) {
                for (event in events) {
                    sendNotification(event)
                }
            }
            return
        }

        var observable: Single<List<NotificationEvent>>? = null
        if (NotificationEvent.fromQms(source)) {
            observable = Single.fromCallable { eventsApi.qmsEvents }
        } else if (NotificationEvent.fromTheme(source)) {
            observable = Single.fromCallable { eventsApi.favoritesEvents }
        }

        if (observable != null) {
            observable
                    .onErrorReturnItem(emptyList())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ loadedEvents ->

                        val savedEvents = getSavedEvents(source)
                        savedEvents.forEach { event ->
                            //Log.e("events_lalala", "check saved events " + event.sourceEventText + " : " + event.source + " : " + event.sourceTitle + " : " + event.userNick)
                        }
                        //savedEvents = mutableListOf();
                        saveEvents(loadedEvents, source)
                        val newEvents = compareEvents(savedEvents, loadedEvents, events, source)
                        newEvents.forEach { event ->
                            //Log.e("events_lalala", "check new events " + event.sourceEventText + " : " + event.source + " : " + event.sourceTitle + " : " + event.userNick)
                        }
                        val stackedNewEvents = newEvents.toMutableList()

                        checkOldEvents(loadedEvents, source)

                        //Удаляем из общего уведомления текущие уведомление
                        for (event in events) {
                            for (newEvent in newEvents) {
                                if (newEvent.sourceId == event.sourceId) {
                                    stackedNewEvents.remove(newEvent)
                                    newEvent.type = event.type
                                    newEvent.messageId = event.messageId

                                    notifyTabs(TabNotification(
                                            newEvent.source,
                                            newEvent.type,
                                            newEvent,
                                            false,
                                            loadedEvents.toList(),
                                            newEvents.toList()
                                    ))

                                    sendNotification(newEvent)
                                } else if (event.isMention && !notificationPreferencesHolder.getFavEnabled()) {
                                    stackedNewEvents.remove(newEvent)
                                }
                            }
                        }

                        sendNotifications(stackedNewEvents, source)
                    }, {
                        it.printStackTrace()
                    })
        }
    }


    private fun handlePendingEvents(source: NotificationEvent.Source) {
        val pending = pendingEvents[source]
        if (pending != null && pending.isNotEmpty()) {
            hardHandleEvent(pending.map { it.value }, source)
            pending.clear()
        }
    }


    private fun getSavedEvents(source: NotificationEvent.Source): List<NotificationEvent> {
        val savedEvents: Set<String> = when {
            NotificationEvent.fromQms(source) -> notificationPreferencesHolder.getDataQmsEvents()
            NotificationEvent.fromTheme(source) -> notificationPreferencesHolder.getDataFavoritesEvents()
            else -> return emptyList()
        }

        val responseBuilder = StringBuilder()
        for (saved in savedEvents) {
            responseBuilder.append(saved).append('\n')
        }
        val response = responseBuilder.toString()

        if (NotificationEvent.fromQms(source)) {
            return eventsApi.getQmsEvents(response)
        } else if (NotificationEvent.fromTheme(source)) {
            return eventsApi.getFavoritesEvents(response)
        }
        return emptyList()
    }

    private fun saveEvents(loadedEvents: List<NotificationEvent>, source: NotificationEvent.Source) {
        val savedEvents = androidx.collection.ArraySet<String>()
        for (event in loadedEvents) {
            savedEvents.add(event.sourceEventText)
        }
        if (NotificationEvent.fromQms(source)) {
            notificationPreferencesHolder.setDataQmsEvents(savedEvents)
        } else if (NotificationEvent.fromTheme(source)) {
            notificationPreferencesHolder.setDataFavoritesEvents(savedEvents)
        }
    }

    private fun compareEvents(
            savedEvents: List<NotificationEvent>,
            loadedEvents: List<NotificationEvent>,
            events: List<NotificationEvent>,
            source: NotificationEvent.Source
    ): List<NotificationEvent> {
        val newEvents = mutableListOf<NotificationEvent>()

        for (loaded in loadedEvents) {
            var isNew = true
            for (saved in savedEvents) {
                if (loaded.sourceId == saved.sourceId && loaded.timeStamp <= saved.timeStamp) {
                    isNew = false
                    break
                }
            }

            if (isNew) {
                newEvents.add(loaded)
            }
        }

        if (NotificationEvent.fromTheme(source) && notificationPreferencesHolder.getFavOnlyImportant()) {
            val toRemove = mutableListOf<NotificationEvent>()
            for (newEvent in newEvents) {
                var remove = false
                for (event in events) {
                    if (!event.isMention && !newEvent.isImportant) {
                        remove = true
                        break
                    }
                }
                if (!newEvent.isImportant) {
                    remove = true
                }
                if (remove) {
                    toRemove.add(newEvent)
                }
            }
            for (removeEvent in toRemove) {
                newEvents.remove(removeEvent)
            }
            toRemove.clear()
        }

        return newEvents
    }


}