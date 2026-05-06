package forpdateam.ru.forpda.model.preferences

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import forpdateam.ru.forpda.common.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow

class ListsPreferencesHolder(
    private val sharedPreferences: SharedPreferences
) {

    private val rxPreferences = RxSharedPreferences.create(sharedPreferences)

    private val unreadTop by lazy {
        rxPreferences.getBoolean(Preferences.Lists.Topic.UNREAD_TOP, false)
    }

    private val showDot by lazy {
        rxPreferences.getBoolean(Preferences.Lists.Topic.SHOW_DOT, false)
    }

    private val favLoadAll by lazy {
        rxPreferences.getBoolean(Preferences.Lists.Favorites.LOAD_ALL, false)
    }

    private val favSortingKey by lazy {
        rxPreferences.getString(Preferences.Lists.Favorites.SORTING_KEY, "")
    }

    private val favSortingOrder by lazy {
        rxPreferences.getString(Preferences.Lists.Favorites.SORTING_ORDER, "")
    }

    fun observeUnreadTop(): Flow<Boolean> = unreadTop.asObservable().asFlow()

    fun observeShowDot(): Flow<Boolean> = showDot.asObservable().asFlow()

    fun observeFavLoadAll(): Flow<Boolean> = favLoadAll.asObservable().asFlow()

    fun observeSortingKey(): Flow<String> = favSortingKey.asObservable().asFlow()

    fun observeSortingOrder(): Flow<String> = favSortingOrder.asObservable().asFlow()

    fun setSortingKey(key: String): Unit = favSortingKey.set(key)

    fun setSortingOrder(order: String): Unit = favSortingOrder.set(order)

    fun getUnreadTop(): Boolean = unreadTop.get()

    fun getShowDot(): Boolean = showDot.get()

    fun getFavLoadAll(): Boolean = favLoadAll.get()

    fun getSortingKey(): String = favSortingKey.get()

    fun getSortingOrder(): String = favSortingOrder.get()
}