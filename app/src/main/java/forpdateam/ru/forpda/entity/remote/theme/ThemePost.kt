package forpdateam.ru.forpda.entity.remote.theme

import forpdateam.ru.forpda.entity.remote.BaseForumPost

/**
 * Created by radiationx on 04.08.16.
 */
class ThemePost : BaseForumPost() {
    var forumId: Int = 0
    var number: Int = 0
    val attachImages = ArrayList<Pair<String, String>>()
}
