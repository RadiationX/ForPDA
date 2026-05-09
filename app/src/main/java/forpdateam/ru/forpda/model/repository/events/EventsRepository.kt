package forpdateam.ru.forpda.model.repository.events

import android.util.Log
import androidx.collection.ArraySet
import forpdateam.ru.forpda.client.WebSocketController
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.events.NotificationEventsApi
import forpdateam.ru.forpda.model.data.remote.api.events.NotificationEventsParser
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.Response
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.minutes

class EventsRepository(
    private val webClient: IWebClient,
    private val eventsApi: NotificationEventsApi,
    private val eventsParser: NotificationEventsParser,
    private val authHolder: AuthHolder,
    private val notificationPreferencesHolder: NotificationPreferencesHolder
) {
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

    private var checkTimerJob: Job? = null

    private val eventsHistory = mutableMapOf<Int, NotificationEvent>()

    private val notifyFlow = MutableSharedFlow<NotificationEvent>()
    private val notifyStackFlow = MutableSharedFlow<List<NotificationEvent>>()
    private val cancelFlow = MutableSharedFlow<NotificationEvent>()

    private val controllerListener: WebSocketController.Listener =
        object : WebSocketController.Listener() {
            override fun onConnected() {
                Log.d(LOG_TAG, "WSContr onConnected")
                webSocketController.send("""[${webSocketController.getCurrentId()}, "sv"]""")
                webSocketController.send("""[0, "ea", "u${authHolder.get().userId}"]""")
            }

            override fun onMessage(text: String) {
                Log.d(LOG_TAG, "WSContr onMessage $text")
                try {
                    eventsParser.parseWebSocketEvent(text)?.also {
                        if (it.type != NotificationEvent.Type.HAT_EDITED) {
                            GlobalScope.launch(Dispatchers.Main) {
                                handleWebSocketEvent(it)
                            }
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            override fun onDisconnected(throwable: Throwable, response: Response?) {
                Log.d(LOG_TAG, "WSContr onDisconnected $response", throwable)
                if (throwable is SocketTimeoutException || throwable is TimeoutException) {
                    Log.d(LOG_TAG, "start onFailure")
                    GlobalScope.launch(Dispatchers.Main) {
                        start(true)
                    }
                }
            }
        }

    private val webSocketController = WebSocketController(webClient, controllerListener)

    init {
        authHolder
            .observe()
            .distinctUntilChanged()
            .onEach {
                Log.e("kulolo", "events rep observe authHolder ${it.state}")
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
            .launchIn(GlobalScope)

        flow {
            while (true) {
                emit(webSocketController.isConnected())
                delay(1.minutes)
            }
        }.onEach {
            if (!it) {
                stop()
                start(false)
            }
        }.launchIn(GlobalScope)
        timerPeriod = notificationPreferencesHolder.mainPeriodDuration.get().inWholeMilliseconds
    }

    fun observeEvents(): Flow<NotificationEvent> = notifyFlow

    fun observeEventsStack(): Flow<List<NotificationEvent>> = notifyStackFlow

    fun observeCancel(): Flow<NotificationEvent> = cancelFlow


    fun setTimerPeriod(period: Long) {
        timerPeriod = period
        resetTimer()
    }

    suspend fun externalStart(checkEvents: Boolean) {
        Log.e(LOG_TAG, "start externalStart")
        start(checkEvents)
    }

    suspend fun updateEvents(source: NotificationEvent.Source) {
        hardHandleEvent(source)
    }

    private suspend fun start(checkEvents: Boolean) {
        Log.e(
            LOG_TAG,
            "Start: ${webSocketController.isConnected()} : $checkEvents : ${webSocketController.getCurrentId()}"
        )
        if (authHolder.get().isAuth()) {
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
        webSocketController.disconnect()
    }

    private fun resetTimer() {
        cancelTimer()
        checkTimerJob = flow {
            while (true) {
                emit(Unit)
                delay(timerPeriod)
            }
        }.onEach {
            for (source in pendingEvents.keys) {
                handlePendingEvents(source)
            }
        }.launchIn(GlobalScope)
    }

    private fun cancelTimer() {
        checkTimerJob?.cancel()
        checkTimerJob = null
    }

    private suspend fun sendNotification(event: NotificationEvent) {
        Log.e(
            "events_lalala",
            "send notification rep " + event.sourceEventText + " : " + event.source + " : " + event.sourceTitle + " : " + event.user?.nick
        )
        if (event.user?.id == authHolder.get().userId) {
            return
        }
        eventsHistory[event.notifyId()] = event
        if (!checkNotify(event, event.source)) {
            return
        }
        notifyFlow.emit(event)
    }

    private suspend fun sendNotifications(
        events: List<NotificationEvent>,
        tSource: NotificationEvent.Source
    ) {
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
        notifyStackFlow.emit(events)
    }

    private fun checkNotify(event: NotificationEvent?, source: NotificationEvent.Source): Boolean {
        if (!notificationPreferencesHolder.mainEnabled.get()) {
            return false
        }
        if (NotificationEvent.fromQms(source)) {
            if (!notificationPreferencesHolder.qmsEnabled.get()) {
                return false
            }
        } else if (NotificationEvent.fromTheme(source)) {
            if (event != null && event.isMention) {
                if (!notificationPreferencesHolder.mentionsEnabled.get()) {
                    return false
                }
            } else {
                if (!notificationPreferencesHolder.favEnabled.get()) {
                    return false
                }
            }
        }
        return true
    }

    private suspend fun checkOldEvent(event: NotificationEvent) {
        var oldEvent = eventsHistory[event.notifyId(NotificationEvent.Type.NEW)]
        var delete = false

        Log.e("kulolo", "checkOldEvent \n${oldEvent} \n$event")

        if (event.fromTheme()) {
            //Убираем уведомления избранного
            if (oldEvent != null && event.messageId >= oldEvent.messageId) {
                cancelFlow.emit(oldEvent)
                delete = true
            }

            //Убираем уведомление упоминаний
            oldEvent = eventsHistory[event.notifyId(NotificationEvent.Type.MENTION)]
            if (oldEvent != null) {
                cancelFlow.emit(oldEvent)
                delete = true
            }
        } else if (event.fromQms()) {

            //Убираем уведомление кумыса
            if (oldEvent != null) {
                cancelFlow.emit(oldEvent)
                delete = true
            }
        }

    }

    private suspend fun checkOldEvents(
        loadedEvents: List<NotificationEvent>,
        source: NotificationEvent.Source
    ) {
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
                cancelFlow.emit(oldEvent)
                eventsHistory.remove(oldEvent.notifyId(NotificationEvent.Type.NEW))

            }
        }
    }

    private suspend fun handleWebSocketEvent(event: NotificationEvent) {
        if (event.isRead) {
            checkOldEvent(event)
            return
        }
        eventsHistory[event.notifyId()] = event

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

    private suspend fun hardHandleEvent(source: NotificationEvent.Source) {
        hardHandleEvent(emptyList(), source)
    }

    private suspend fun hardHandleEvent(
        events: List<NotificationEvent>,
        source: NotificationEvent.Source
    ) {
        Log.d("SUKA", "hardHandleEvent " + events.size + " : " + source)
        if (NotificationEvent.fromSite(source)) {
            if (notificationPreferencesHolder.mentionsEnabled.get()) {
                for (event in events) {
                    sendNotification(event)
                }
            }
            return
        }


        val loadedEvents = coRunCatching {
            when {
                NotificationEvent.fromQms(source) -> eventsApi.getQmsEvents()
                NotificationEvent.fromTheme(source) -> eventsApi.getFavoritesEvents()
                else -> null
            }
        }.getOrNull()

        if (loadedEvents != null) {
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
                        val eventToSend = newEvent.copy(
                            type = event.type,
                            messageId = event.messageId
                        )

                        sendNotification(eventToSend)
                    } else if (event.isMention && !notificationPreferencesHolder.favEnabled.get()) {
                        stackedNewEvents.remove(newEvent)
                    }
                }
            }

            sendNotifications(stackedNewEvents, source)
        }
    }

    private suspend fun handlePendingEvents(source: NotificationEvent.Source) {
        val pending = pendingEvents[source]
        if (pending != null && pending.isNotEmpty()) {
            hardHandleEvent(pending.map { it.value }, source)
            pending.clear()
        }
    }


    private fun getSavedEvents(source: NotificationEvent.Source): List<NotificationEvent> {
        val savedEvents: Set<String> = when {
            NotificationEvent.fromQms(source) -> notificationPreferencesHolder.dataQmsEvents.get()
            NotificationEvent.fromTheme(source) -> notificationPreferencesHolder.dataFavoritesEvents.get()
            else -> null
        } ?: return emptyList()

        val responseBuilder = StringBuilder()
        for (saved in savedEvents) {
            responseBuilder.append(saved).append('\n')
        }
        val response = responseBuilder.toString()

        if (NotificationEvent.fromQms(source)) {
            return eventsParser.parseQmsEvents(response)
        } else if (NotificationEvent.fromTheme(source)) {
            return eventsParser.parseFavoritesEvents(response)
        }
        return emptyList()
    }

    private fun saveEvents(
        loadedEvents: List<NotificationEvent>,
        source: NotificationEvent.Source
    ) {
        val savedEvents = ArraySet<String>()
        for (event in loadedEvents) {
            savedEvents.add(event.sourceEventText)
        }
        if (NotificationEvent.fromQms(source)) {
            notificationPreferencesHolder.dataQmsEvents.set(savedEvents)
        } else if (NotificationEvent.fromTheme(source)) {
            notificationPreferencesHolder.dataFavoritesEvents.set(savedEvents)
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

        if (NotificationEvent.fromTheme(source) && notificationPreferencesHolder.favOnlyImportant.get()) {
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