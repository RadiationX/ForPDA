package forpdateam.ru.forpda.entity.remote.theme

import forpdateam.ru.forpda.entity.DeferredData
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import ru.radiationx.coretypes.FavoriteId
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.links.Link

/**
 * Created by radiationx on 04.08.16.
 */
data class ThemePage(
    val id: TopicId,
    val title: String,
    val desc: String,
    val forumId: ForumId,
    // todo use only favid
    @Deprecated("use favid after refactoring")
    val isInFavorite: Boolean,
    val favId: FavoriteId?,
    val canQuote: Boolean,
    val posts: List<ThemePost>,
    val pagination: Pagination,
    val poll: Poll?,
    val html: DeferredData<String>?,
    val link: Link.Board.Topic.ShowTopic.Page,
    val isHatOpen: Boolean,
    val isPollOpen: Boolean,
    val scrollY: Int,
    val anchors: List<Link.Board.Topic.Anchor>
) {
    val anchor: Link.Board.Topic.Anchor?
        get() = anchors.lastOrNull()

    val st: Int
        get() = pagination.currentPage()

}
