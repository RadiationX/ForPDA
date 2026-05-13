package forpdateam.ru.forpda.ui.views.messagepanel.advanced

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Created by radiationx on 08.01.17.
 */
sealed interface PanelListItem {
    data class Smile(
        val text: String,
        val assetFileName: String
    ) : PanelListItem

    data class BBCode(
        val text: String,
        @param:DrawableRes val iconRes: Int,
        @param:StringRes val titleRes: Int
    ) : PanelListItem

    data class Color(
        val hexColor: String,
        @param:ColorInt val color: Int,
    ) : PanelListItem


}
