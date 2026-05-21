package forpdateam.ru.forpda.entity.remote.theme

import forpdateam.ru.forpda.entity.DeferredData
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 04.08.16.
 */
data class ThemePage(
    val id: Int,
    val title: String,
    val desc: String,
    val forumId: Int,
    // todo use only favid
    @Deprecated("use favid after refactoring")
    val isInFavorite: Boolean,
    val favId: Int?,
    val canQuote: Boolean,
    val posts: List<ThemePost>,
    val pagination: Pagination,
    val poll: Poll?,
    val html: DeferredData<String>?,
    val url: TopicUrl.ShowTopic.Page,
    val isHatOpen: Boolean,
    val isPollOpen: Boolean,
    val scrollY: Int,
    val anchors: List<TopicUrl.Anchor>
) {
    val anchor: TopicUrl.Anchor?
        get() = anchors.lastOrNull()

    val st: Int
        get() = pagination.currentPage()

}
