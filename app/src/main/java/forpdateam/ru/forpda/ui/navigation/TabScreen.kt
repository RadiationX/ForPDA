package forpdateam.ru.forpda.ui.navigation

import forpdateam.ru.forpda.presentation.Screen

class TabScreen(var key: String) {

    companion object {
        fun fromScreen(screen: Screen) = TabScreen(screen.getKey()).apply {
            screenTitle = screen.screenTitle
            screenSubTitle = screen.screenSubTitle
            fromMenu = screen.fromMenu
            isAlone = screen.isAlone
        }
    }
    var screenTitle: String? = null
    var screenSubTitle: String? = null
    var fromMenu: Boolean = false
    var isAlone: Boolean = false
}