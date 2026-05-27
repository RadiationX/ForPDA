package forpdateam.ru.forpda.entity.remote.favorites

import ru.radiationx.coretypes.FavoriteId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.TopicId

sealed interface FavoriteAction {
    data class AddTopic(val topicId: TopicId, val trackType: String) : FavoriteAction
    data class AddForum(val forumId: ForumId, val trackType: String) : FavoriteAction
    data class Delete(val favoriteId: FavoriteId) : FavoriteAction
    data class EditPinState(val favoriteId: FavoriteId, val state: String) : FavoriteAction
    data class EditTrackType(val favoriteId: FavoriteId, val trackType: String) : FavoriteAction
}