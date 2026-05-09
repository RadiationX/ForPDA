package forpdateam.ru.forpda.model.interactors.events.handlers

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.entity.remote.inspector.InspectorDiff
import forpdateam.ru.forpda.entity.remote.inspector.InspectorItem
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache
import java.sql.Date

class FavoritesEventsHandler(
    private val favoritesCache: FavoritesCache
) {

    suspend fun handle(event: WebSocketEvent) {
        if (event !is WebSocketEvent.Favorite) {
            return
        }
        updateItem(event.topicId) { favItem ->
            when (event.type) {
                WebSocketEvent.Favorite.Type.New -> favItem.copy(
                    isNew = true,
                    date = Utils.getForumDateTime(Date(event.timeStamp))
                )

                WebSocketEvent.Favorite.Type.Read -> favItem.copy(isNew = false)
                WebSocketEvent.Favorite.Type.Mention -> favItem
                WebSocketEvent.Favorite.Type.HatUpdate -> favItem
            }
        }
    }

    suspend fun handle(diff: InspectorDiff<InspectorItem.Favorite>) {
        diff.diff.forEach { diffItem ->
            val inspectorItem = diffItem.item
            updateItem(inspectorItem.topicId) { favItem ->
                favItem.copy(
                    isNew = when (diffItem) {
                        is InspectorDiff.Item.New -> true
                        is InspectorDiff.Item.Same -> true
                        is InspectorDiff.Item.Read -> false
                    },
                    lastUser = inspectorItem.user,
                    isPin = inspectorItem.isImportant,
                    date = Utils.getForumDateTime(Date(inspectorItem.timeStamp))
                )
            }
        }
    }

    private suspend fun updateItem(topicId: Int, block: (FavItem) -> FavItem) {
        val item = favoritesCache.getItemByTopicId(topicId) ?: return
        val newItem = block.invoke(item)
        if (newItem == item) {
            return
        }
        favoritesCache.updateItem(newItem)
    }
}