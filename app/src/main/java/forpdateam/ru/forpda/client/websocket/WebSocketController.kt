package forpdateam.ru.forpda.client.websocket

import android.util.Log
import forpdateam.ru.forpda.client.NetworkObserver
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.IWebClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import okhttp3.WebSocket
import kotlin.time.Duration.Companion.milliseconds

class WebSocketController(
    private val webClient: IWebClient,
    private val authHolder: AuthHolder,
    private val networkObserver: NetworkObserver
) {

    companion object {
        private const val LOG_TAG = "WebSocketControllerNew"
    }

    private var sessionJob: Job? = null

    private val emptyWebSocketFlow = MutableSharedFlow<WebSocketFlow.Event>().asSharedFlow()

    private val webSocketFlow = WebSocketFlow(webClient::createWebSocketConnection)
        .retryWhen { cause, attempt ->
            Log.d(LOG_TAG, "retryWhen attempt=$attempt", cause)
            connectionState.value = ConnectionState.Connecting
            delay(1000 * (attempt + 1))
            true
        }
        .onStart {
            Log.d(LOG_TAG, "onStart")
            connectionState.value = ConnectionState.Connecting
        }
        .onCompletion { cause ->
            Log.d(LOG_TAG, "onCompletion", cause)
            connectionState.value = ConnectionState.Disconnected
        }
        .onEach { event ->
            Log.d(LOG_TAG, "onEach $event")
            when (event) {
                is WebSocketFlow.Event.Open -> {
                    val sessionId = (1000..16384).random()
                    event.webSocket.send("""[$sessionId, "sv"]""")
                    event.webSocket.send("""[0, "ea", "u${authHolder.get().userId}"]""")
                    connectionState.value = ConnectionState.Connected(sessionId, event.webSocket)
                }

                is WebSocketFlow.Event.TextMessage -> {
                    messagesFlow.emit(event.text)
                }
            }
        }
        .shareIn(GlobalScope, SharingStarted.WhileSubscribed(100.milliseconds))

    private val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    private val messagesFlow = MutableSharedFlow<String>()

    fun start() {
        if (sessionJob?.isActive == true) {
            return
        }
        sessionJob = combine(
            messagesFlow.subscriptionCount.map { it > 0 },
            authHolder.observe().map { it.isAuth() },
            networkObserver.observeAvailable()
        ) { (hasSubscribers, hasAuth, hasNetwork) ->
            Log.d(LOG_TAG, "session combine $hasSubscribers, $hasAuth, $hasNetwork")
            hasSubscribers && hasAuth && hasNetwork
        }.flatMapLatest {
            if (it) {
                webSocketFlow
            } else {
                emptyWebSocketFlow
            }
        }.launchIn(GlobalScope)
    }

    fun stop() {
        sessionJob?.cancel()
        sessionJob = null
    }

    fun observeMessages(): Flow<String> = messagesFlow

    suspend fun sendMessage(message: String) {
        val session = connectionState.filterIsInstance<ConnectionState.Connected>().first()
        session.webSocket.send(message)
    }

    sealed interface ConnectionState {
        data object Disconnected : ConnectionState
        data object Connecting : ConnectionState
        class Connected(val sessionId: Int, val webSocket: WebSocket) : ConnectionState
    }
}