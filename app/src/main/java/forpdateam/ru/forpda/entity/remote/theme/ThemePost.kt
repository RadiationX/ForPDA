package forpdateam.ru.forpda.entity.remote.theme

import forpdateam.ru.forpda.entity.remote.BaseForumPost

/**
 * Created by radiationx on 04.08.16.
 */
class ThemePost : BaseForumPost(), IThemePost {
    val attachImages = ArrayList<Pair<String, String>>()
}
