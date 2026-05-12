package forpdateam.ru.forpda.ui.views.adapters

import androidx.annotation.StringRes
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.SectionListItem

class SectionsBuilder {

    private val _items = mutableListOf<ListItem>()

    fun addItem(item: ListItem) {
        _items.add(item)
    }

    fun addItems(items: List<ListItem>) {
        _items.addAll(items)
    }

    fun addSection(title: String, items: List<ListItem>) {
        if (items.isEmpty()) return
        addItem(SectionListItem.String(title, _items.isNotEmpty()))
        addItems(items)
    }

    fun addSection(@StringRes titleRes: Int, items: List<ListItem>) {
        if (items.isEmpty()) return
        addItem(SectionListItem.Res(titleRes, _items.isNotEmpty()))
        addItems(items)
    }

    fun <T> addSection(title: String, items: List<T>, transform: (T) -> ListItem) {
        if (items.isEmpty()) return
        addItem(SectionListItem.String(title, _items.isNotEmpty()))
        items.forEach {
            addItem(transform(it))
        }
    }

    fun <T> addSection(@StringRes titleRes: Int, items: List<T>, transform: (T) -> ListItem) {
        if (items.isEmpty()) return
        addItem(SectionListItem.Res(titleRes, _items.isNotEmpty()))
        items.forEach {
            addItem(transform(it))
        }
    }

    fun build(): List<ListItem> {
        return _items
    }
}

fun buildSections(block: SectionsBuilder.() -> Unit): List<ListItem> {
    val builder = SectionsBuilder()
    block.invoke(builder)
    return builder.build()
}
