package forpdateam.ru.forpda.model.interactors.events.handlers

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.inspector.InspectorDiff
import forpdateam.ru.forpda.model.data.cache.favorites.FavoritesCache
import ru.radiationx.coretypes.TopicId
import java.sql.Date
import javax.inject.Inject

class FavoritesEventsHandler @Inject constructor(
    private val favoritesCache: FavoritesCache,
    private val utils: Utils
) {

    suspend fun handle(event: WebSocketEvent) {
        if (event !is WebSocketEvent.Topic) {
            return
        }
        updateItem(event.topicId) { favItem ->
            when (event.type) {
                is WebSocketEvent.Topic.Type.New -> favItem.copy(
                    isNew = true,
                    date = utils.getForumDateTime(Date(event.timeStamp))
                )

                is WebSocketEvent.Topic.Type.Read -> favItem
                is WebSocketEvent.Topic.Type.Mention -> favItem
                is WebSocketEvent.Topic.Type.HatUpdate -> favItem
            }
        }
    }

    suspend fun handle(diff: InspectorDiff.Favorites) {
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
                    date = utils.getForumDateTime(Date(inspectorItem.timeStamp))
                )
            }
        }
    }

    private suspend fun updateItem(topicId: TopicId, block: (Favorite.Topic) -> Favorite.Topic) {
        val item = favoritesCache.getItemByTopicId(topicId) ?: return
        val newItem = block.invoke(item)
        if (newItem == item) {
            return
        }
        favoritesCache.updateItem(newItem)
    }
}