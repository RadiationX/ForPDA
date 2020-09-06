package forpdateam.ru.forpda.ui.navigation

import forpdateam.ru.forpda.presentation.Screen

class TabItem {
    var tag: String = ""
    var screen: TabScreen? = null
    var parent: TabItem? = null
    val children = mutableListOf<TabItem>()
}