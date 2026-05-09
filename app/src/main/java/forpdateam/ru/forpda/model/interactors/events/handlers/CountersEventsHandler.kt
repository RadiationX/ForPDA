package forpdateam.ru.forpda.model.interactors.events.handlers

import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.inspector.InspectorDiff
import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.entity.remote.inspector.InspectorMention
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

    fun handle(mention: InspectorMention){
        handleCounterEvent(CounterEvent.MentionState(mention.count))
    }

    private fun handleCounterEvent(event: CounterEvent) {
        countersHolder.update { counters ->
            when (event) {
                CounterEvent.TopicMention -> counters.copy(mentions = counters.mentions + 1)
                is CounterEvent.FavoriteState -> counters.copy(favorites = event.count)
                CounterEvent.QmsNew -> counters.copy(qms = counters.qms + 1)
                CounterEvent.QmsReadAll -> counters.copy(qms = counters.qms - 1)
                is CounterEvent.QmsState -> counters.copy(qms = event.count)
                CounterEvent.SiteMention -> counters.copy(mentions = counters.mentions + 1)
                is CounterEvent.MentionState -> counters.copy(mentions = counters.mentions)
            }
        }
    }

    private fun WebSocketEvent.toCounterEvent(): CounterEvent? {
        return when (this) {
            is WebSocketEvent.Topic -> when (type) {
                is WebSocketEvent.Topic.Type.HatUpdate -> null
                is WebSocketEvent.Topic.Type.Mention -> CounterEvent.TopicMention
                is WebSocketEvent.Topic.Type.New -> null
                is WebSocketEvent.Topic.Type.Read -> null
            }

            is WebSocketEvent.Forum -> null
            is WebSocketEvent.Qms -> when (type) {
                is WebSocketEvent.Qms.Type.New -> CounterEvent.QmsNew
                is WebSocketEvent.Qms.Type.Read -> null
                is WebSocketEvent.Qms.Type.ReadAll -> CounterEvent.QmsReadAll
                WebSocketEvent.Qms.Type.Typing -> null
                WebSocketEvent.Qms.Type.Uploading -> null
            }

            is WebSocketEvent.Site -> when (type) {
                is WebSocketEvent.Site.Type.Mention -> CounterEvent.SiteMention
                is WebSocketEvent.Site.Type.Read -> null
            }
        }
    }

    private fun InspectorDiff<InspectorItem.Favorite>.toCounterEvent(): CounterEvent {
        return CounterEvent.FavoriteState(loadedItems.size)
    }

    private fun InspectorDiff<InspectorItem.Qms>.toCounterEvent(): CounterEvent {
        return CounterEvent.QmsState(loadedItems.sumOf { it.msgCount })
    }

    private sealed interface CounterEvent {
        data object TopicMention : CounterEvent
        data class FavoriteState(val count: Int) : CounterEvent
        data object QmsNew : CounterEvent
        data object QmsReadAll : CounterEvent
        data class QmsState(val count: Int) : CounterEvent
        data object SiteMention : CounterEvent
        data class MentionState(val count: Int) : CounterEvent
    }
}