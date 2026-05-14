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
    val url: String,
    val isHatOpen: Boolean,
    val isPollOpen: Boolean,
    val scrollY: Int,
    val anchors: List<String>
) {
    val anchor: String?
        get() = if (anchors.isEmpty()) null else anchors[anchors.size - 1]

    val st: Int
        get() = pagination.currentPage()

}
