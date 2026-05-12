package forpdateam.ru.forpda.ui.fragments.favorites

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.adapters.SectionItemDelegate
import forpdateam.ru.forpda.ui.views.adapters.buildSections
import forpdateam.ru.forpda.ui.views.drawers.adapters.FavoriteListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class FavoritesAdapter(
    private val favoriteClickListener: OnItemClickListener<FavItem>,
) : ListDelegationAdapter<List<ListItem>>() {

    private var rawItems = emptyList<FavItem>()
    private var showDot = false
    private var unreadTop = false

    init {
        delegatesManager.apply {
            addDelegate(SectionItemDelegate())
            addDelegate(FavoriteDelegate(favoriteClickListener))
        }
    }

    fun setShowDot(showDot: Boolean) {
        this.showDot = showDot
        bindItems(rawItems)
    }

    fun setUnreadTop(unreadTop: Boolean) {
        this.unreadTop = unreadTop
        bindItems(rawItems)
    }

    fun bindItems(items: List<FavItem>) {
        rawItems = items
        this.items = buildSections {
            val pinnedUnread = mutableListOf<FavItem>()
            val itemsUnread = mutableListOf<FavItem>()
            val pinned = mutableListOf<FavItem>()
            val otherItems = mutableListOf<FavItem>()
            for (item in items) {
                if (item.isPin) {
                    if (unreadTop && item.isNew) {
                        pinnedUnread.add(item)
                    } else {
                        pinned.add(item)
                    }
                } else {
                    if (unreadTop && item.isNew) {
                        itemsUnread.add(item)
                    } else {
                        otherItems.add(item)
                    }
                }
            }

            addSection(R.string.fav_unreaded_pinned, pinnedUnread) {
                FavoriteListItem(it, showDot)
            }
            addSection(R.string.fav_unreaded, itemsUnread) {
                FavoriteListItem(it, showDot)
            }
            addSection(R.string.fav_pinned, pinned) {
                FavoriteListItem(it, showDot)
            }
            addSection(R.string.fav_themes, otherItems) {
                FavoriteListItem(it, showDot)
            }
        }
        notifyDataSetChanged()
    }
}