package forpdateam.ru.forpda.model.preferences

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import forpdateam.ru.forpda.common.Preferences
import io.reactivex.Observable

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

    fun observeUnreadTop(): Observable<Boolean> = unreadTop.asObservable()

    fun observeShowDot(): Observable<Boolean> = showDot.asObservable()

    fun observeFavLoadAll(): Observable<Boolean> = favLoadAll.asObservable()

    fun observeSortingKey(): Observable<String> = favSortingKey.asObservable()

    fun observeSortingOrder(): Observable<String> = favSortingOrder.asObservable()

    fun setSortingKey(key: String): Unit = favSortingKey.set(key)

    fun setSortingOrder(order: String): Unit = favSortingOrder.set(order)

    fun getUnreadTop(): Boolean = unreadTop.get()

    fun getShowDot(): Boolean = showDot.get()

    fun getFavLoadAll(): Boolean = favLoadAll.get()

    fun getSortingKey(): String = favSortingKey.get()

    fun getSortingOrder(): String = favSortingOrder.get()
}