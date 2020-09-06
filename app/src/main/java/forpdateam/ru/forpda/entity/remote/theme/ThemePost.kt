package forpdateam.ru.forpda.entity.remote.theme

import android.util.Pair

import java.util.ArrayList

import forpdateam.ru.forpda.entity.remote.BaseForumPost

/**
 * Created by radiationx on 04.08.16.
 */
class ThemePost : BaseForumPost(), IThemePost {
    val attachImages = ArrayList<Pair<String, String>>()
}
