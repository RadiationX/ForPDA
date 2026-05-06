package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.flowpreferences.FlowPreferences
import kotlinx.coroutines.flow.Flow

class ListsPreferencesHolder(
    private val preferences: FlowPreferences
) {


    private val unreadTop by lazy {
        preferences.getBoolean(Preferences.Lists.Topic.UNREAD_TOP, false)
    }

    private val showDot by lazy {
        preferences.getBoolean(Preferences.Lists.Topic.SHOW_DOT, false)
    }

    private val favLoadAll by lazy {
        preferences.getBoolean(Preferences.Lists.Favorites.LOAD_ALL, false)
    }

    private val favSortingKey by lazy {
        preferences.getString(Preferences.Lists.Favorites.SORTING_KEY)
    }

    private val favSortingOrder by lazy {
        preferences.getString(Preferences.Lists.Favorites.SORTING_ORDER)
    }

    fun observeUnreadTop(): Flow<Boolean> = unreadTop

    fun observeShowDot(): Flow<Boolean> = showDot

    fun observeFavLoadAll(): Flow<Boolean> = favLoadAll

    fun observeSortingKey(): Flow<String?> = favSortingKey

    fun observeSortingOrder(): Flow<String?> = favSortingOrder

    fun setSortingKey(key: String): Unit = favSortingKey.set(key)

    fun setSortingOrder(order: String): Unit = favSortingOrder.set(order)

    fun getUnreadTop(): Boolean = unreadTop.get()

    fun getShowDot(): Boolean = showDot.get()

    fun getFavLoadAll(): Boolean = favLoadAll.get()

    fun getSortingKey(): String? = favSortingKey.get()

    fun getSortingOrder(): String? = favSortingOrder.get()
}