package forpdateam.ru.forpda.ui.views.messagepanel.advanced

import androidx.annotation.DrawableRes

/**
 * Created by radiationx on 08.01.17.
 */
class ButtonData {
    @JvmField
    val text: String
    var icon: String? = null
        private set
    var title: String? = null
        private set
    var iconRes: Int = 0
        private set
    var listener: ClickListener? = null
        private set

    interface ClickListener {
        fun onClick(data: ButtonData?)
    }

    constructor(text: String, icon: String?) {
        this.text = text
        this.icon = icon
    }

    constructor(text: String, @DrawableRes iconRes: Int) {
        this.text = text
        this.iconRes = iconRes
    }

    constructor(text: String, @DrawableRes iconRes: Int, title: String?) {
        this.text = text
        this.iconRes = iconRes
        this.title = title
    }

    constructor(text: String, @DrawableRes iconRes: Int, listener: ClickListener?) {
        this.text = text
        this.iconRes = iconRes
        this.listener = listener
    }
}
