package forpdateam.ru.forpda.model

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.app.CloseableInfo
import io.reactivex.Observable

class CloseableInfoHolder(
        private val preferences: SharedPreferences,
        private val schedulers: SchedulersProvider
) {

    companion object {

        const val item_other_menu_drag = 10

        val ALL_ITEMS = arrayOf(
                item_other_menu_drag
        )
    }

    private val relay = BehaviorRelay.create<List<CloseableInfo>>()

    init {
        val closedIds: List<Int> = preferences.getString("closeable_info_closed_ids", null)?.let { savedIds ->
            savedIds.split(',').map { it.toInt() }
        } ?: emptyList()

        val allItems = ALL_ITEMS.map { CloseableInfo(it, closedIds.contains(it)) }
        relay.accept(allItems)
    }

    fun observe(): Observable<List<CloseableInfo>> = relay
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui());

    fun get(): List<CloseableInfo> = relay.value!!

    fun close(item: CloseableInfo) {
        val currentItems = get()
        currentItems.firstOrNull { it.id == item.id }?.isClosed = true
        val closedItems = currentItems.filter { it.isClosed }
        preferences.edit().putString("closeable_info_closed_ids", closedItems.joinToString(",") { it.id.toString() }).apply()
        relay.accept(currentItems)
    }


}