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
    val favId: Int,
    val isInFavorite: Boolean,
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
