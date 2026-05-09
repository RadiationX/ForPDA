package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.flowpreferences.FlowPreferences

class ListsPreferencesHolder(
    private val preferences: FlowPreferences
) {


    val unreadTop by lazy {
        preferences.getBoolean(Preferences.Lists.Topic.UNREAD_TOP, false)
    }

    val showDot by lazy {
        preferences.getBoolean(Preferences.Lists.Topic.SHOW_DOT, false)
    }

    val favLoadAll by lazy {
        preferences.getBoolean(Preferences.Lists.Favorites.LOAD_ALL, false)
    }

    val favSortingKey by lazy {
        preferences.getString(Preferences.Lists.Favorites.SORTING_KEY)
    }

    val favSortingOrder by lazy {
        preferences.getString(Preferences.Lists.Favorites.SORTING_ORDER)
    }

}