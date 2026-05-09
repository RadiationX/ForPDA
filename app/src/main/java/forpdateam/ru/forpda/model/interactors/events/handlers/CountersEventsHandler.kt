package forpdateam.ru.forpda.model.interactors.events.handlers

import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.inspector.InspectorDiff
import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.model.CountersHolder

class CountersEventsHandler(
    private val countersHolder: CountersHolder
) {

    fun handle(event: WebSocketEvent) {
        val counterEvent = event.toCounterEvent() ?: return
        handleCounterEvent(counterEvent)
    }

    fun handle(diff: InspectorDiff<InspectorItem.Favorite>) {
        handleCounterEvent(diff.toCounterEvent())
    }

    fun handle(diff: InspectorDiff<InspectorItem.Qms>) {
        handleCounterEvent(diff.toCounterEvent())
    }

    private fun handleCounterEvent(event: CounterEvent) {
        countersHolder.update { counters ->
            when (event) {
                CounterEvent.FavoriteMention -> counters.copy(mentions = counters.mentions + 1)
                CounterEvent.FavoriteRead -> counters.copy(favorites = counters.favorites - 1)
                is CounterEvent.FavoriteState -> counters.copy(favorites = event.count)
                CounterEvent.QmsNew -> counters.copy(qms = counters.qms + 1)
                CounterEvent.QmsReadAll -> counters.copy(qms = counters.qms - 1)
                is CounterEvent.QmsState -> counters.copy(qms = event.count)
            }
        }
    }

    private fun WebSocketEvent.toCounterEvent(): CounterEvent? {
        return when (this) {
            is WebSocketEvent.Favorite -> {
                when (type) {
                    WebSocketEvent.Favorite.Type.New -> null
                    WebSocketEvent.Favorite.Type.Read -> CounterEvent.FavoriteRead
                    WebSocketEvent.Favorite.Type.Mention -> CounterEvent.FavoriteMention
                    WebSocketEvent.Favorite.Type.HatUpdate -> null
                }
            }

            is WebSocketEvent.Forum -> null
            is WebSocketEvent.QmsAction -> null
            is WebSocketEvent.QmsMessage -> {
                when (type) {
                    WebSocketEvent.QmsMessage.Type.New -> CounterEvent.QmsNew
                    WebSocketEvent.QmsMessage.Type.Read -> null
                    WebSocketEvent.QmsMessage.Type.ReadAll -> CounterEvent.QmsReadAll
                }
            }

            is WebSocketEvent.Site -> null
        }
    }

    private fun InspectorDiff<InspectorItem.Favorite>.toCounterEvent(): CounterEvent {
        return CounterEvent.FavoriteState(loadedItems.size)
    }

    private fun InspectorDiff<InspectorItem.Qms>.toCounterEvent(): CounterEvent {
        return CounterEvent.QmsState(loadedItems.sumOf { it.msgCount })
    }

    private sealed interface CounterEvent {
        data object FavoriteRead : CounterEvent
        data object FavoriteMention : CounterEvent
        data class FavoriteState(val count: Int) : CounterEvent
        data object QmsNew : CounterEvent
        data object QmsReadAll : CounterEvent
        data class QmsState(val count: Int) : CounterEvent
    }
}