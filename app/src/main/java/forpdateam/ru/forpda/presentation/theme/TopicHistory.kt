package forpdateam.ru.forpda.presentation.theme

import forpdateam.ru.forpda.entity.remote.theme.ThemePage

class TopicHistory {

    private var history = listOf<ThemePage>()

    val currentPage: ThemePage?
        get() = history.lastOrNull()

    val size: Int
        get() = history.size

    fun updateOrAdd(block: (ThemePage?) -> ThemePage) {
        update(block.invoke(currentPage))
    }

    fun updateExist(block: (ThemePage) -> ThemePage) {
        val page = currentPage ?: return
        update(block.invoke(page))
    }

    fun update(page: ThemePage) {
        updateHistory {
            if (it.isEmpty()) {
                it.add(page)
            } else {
                it[it.lastIndex] = page
            }
        }
    }

    fun add(page: ThemePage) {
        updateHistory {
            it.add(page)
        }
    }

    fun removeCurrent() {
        updateHistory {
            if (it.isNotEmpty()) {
                it.removeAt(it.lastIndex)
            }
        }
    }

    private fun updateHistory(block: (MutableList<ThemePage>) -> Unit) {
        val newList = history.toMutableList()
        block.invoke(newList)
        history = newList
    }
}