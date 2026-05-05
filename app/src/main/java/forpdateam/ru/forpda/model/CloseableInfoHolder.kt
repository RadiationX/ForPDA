package forpdateam.ru.forpda.model

import android.content.SharedPreferences
import androidx.core.content.edit
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.extensions.replace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class CloseableInfoHolder(
    private val preferences: SharedPreferences
) {

    companion object {

        const val item_other_menu_drag = 10
        const val item_notes_sync = 11

        val ALL_ITEMS = arrayOf(
            item_other_menu_drag,
            item_notes_sync
        )
    }

    private val dataFlow = MutableStateFlow(load())

    fun observe(): Flow<List<CloseableInfo>> = dataFlow

    fun get(): List<CloseableInfo> = dataFlow.value

    fun close(item: CloseableInfo) {
        val currentItems = get().toMutableList()
        currentItems.replace(
            condition = { it.id == item.id },
            map = { it.copy(isClosed = true) }
        )
        val closedItems = currentItems.filter { it.isClosed }
        preferences.edit {
            putString(
                "closeable_info_closed_ids",
                closedItems.joinToString(",") { it.id.toString() }
            )
        }
        dataFlow.value = currentItems
    }

    private fun load(): List<CloseableInfo> {
        val closedIds: List<Int> =
            preferences.getString("closeable_info_closed_ids", null)?.let { savedIds ->
                savedIds.split(',').map { it.toInt() }
            } ?: emptyList()

        return ALL_ITEMS.map { CloseableInfo(it, closedIds.contains(it)) }
    }
}