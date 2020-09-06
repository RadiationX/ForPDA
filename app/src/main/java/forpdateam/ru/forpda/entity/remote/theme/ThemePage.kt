package forpdateam.ru.forpda.entity.remote.theme

import java.util.ArrayList

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 04.08.16.
 */
class ThemePage {
    val anchors = mutableListOf<String>()
    var title: String? = null
    var desc: String? = null
    var html: String? = null
    var url: String? = null
    var id = 0
    var forumId = 0
    var favId = 0
    /*public boolean isCurator() {
        return curator;
    }

    public void setCurator(boolean curator) {
        this.curator = curator;
    }*/

    var scrollY = 0
    var isInFavorite = false
    var isCurator = false
    var canQuote = false
    var isHatOpen = false
    var isPollOpen = false
    val posts = ArrayList<ThemePost>()
    var pagination = Pagination()
    var poll: Poll? = null

    val anchor: String?
        get() = if (anchors.isEmpty()) null else anchors[anchors.size - 1]

    val st: Int
        get() = pagination.current * pagination.perPage

    fun addAnchor(anchor: String): Boolean {
        return anchors.add(anchor)
    }

    fun removeAnchor(): String? {
        return if (anchors.isEmpty()) null else anchors.removeAt(anchors.size - 1)
    }
}
