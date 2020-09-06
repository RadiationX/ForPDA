package forpdateam.ru.forpda.ui.views.drawers.adapters

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter

/**
 * Created by radiationx on 25.02.18.
 */
class BottomMenuAdapter(private val listener: BottomMenuDelegate.Listener) : ListDelegationAdapter<MutableList<ListItem>>() {

    private var currentScreenKey: String? = null

    init {
        items = mutableListOf()
        delegatesManager.run {
            addDelegate(BottomMenuDelegate(listener))
        }
    }

    fun bindItems(menus: List<DrawerMenuItem>) {
        this.items.clear()
        this.items.addAll(menus.map { BottomTabListItem(it) })
        notifyDataSetChanged()
        currentScreenKey?.let { setSelected(it) }
    }

    fun setSelected(screenKey: String) {
        currentScreenKey = screenKey
        items.forEachIndexed { index, item ->
            val listItem = (item as BottomTabListItem)
            val lastSelected = listItem.selected
            listItem.selected = listItem.item.appItem.screen?.getKey() == screenKey
            if (lastSelected != listItem.selected) {
                notifyItemChanged(index)
            }
        }
    }

    interface Listener : BottomMenuDelegate.Listener
}