package forpdateam.ru.forpda.ui.fragments

/**
 * Created by radiationx on 20.03.17.
 */
class TabConfiguration {
    var isAlone: Boolean = false
    var isMenu: Boolean = false
    var isFitSystemWindow: Boolean = false
    var defaultTitle: String = ""

    override fun toString(): String {
        return "TabConfiguration{$isAlone, $isMenu, $defaultTitle}"
    }
}
