package forpdateam.ru.forpda.ui.fragments.other

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.MenuMapper
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.ui.views.drawers.adapters.CloseableInfoListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.DividerShadowListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.MenuListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ProfileListItem
import java.util.Collections

class OtherAdapter(
    private val profileClickListener: (ForumUser?) -> Unit,
    private val logoutClickListener: () -> Unit,
    private val menuClickListener: (DrawerMenuItem) -> Unit,
    private val menuSequenceListener: (List<AppMenuItem>) -> Unit,
    private val infoClickListener: (CloseableInfo) -> Unit
) : ListDelegationAdapter<MutableList<ListItem>>() {

    private val infoCloseClickListener = { item: CloseableInfo ->
        val infoIndex = items.indexOfFirst { it is CloseableInfoListItem && it.item.id == item.id }
        val closeableInfoCount = items.filterIsInstance<CloseableInfoListItem>().size
        if (infoIndex >= 0) {
            items.removeAt(infoIndex)
            if (closeableInfoCount > 1) {
                notifyItemRangeRemoved(infoIndex, 1)
            } else {
                items.removeAt(infoIndex)
                notifyItemRangeRemoved(infoIndex, 2)
            }
        }
        infoClickListener.invoke(item)
    }

    init {
        items = mutableListOf()
        delegatesManager.apply {
            addDelegate(ProfileItemDelegate(profileClickListener, logoutClickListener))
            addDelegate(DividerShadowItemDelegate())
            addDelegate(MenuItemDelegate(menuClickListener))
            addDelegate(CloseableInfoDelegate(infoCloseClickListener))
        }
    }


    fun bindItems(
        user: ForumUser?,
        infoList: List<CloseableInfo>,
        newItems: List<List<AppMenuItem>>
    ) {
        items.clear()

        items.add(ProfileListItem(user))
        items.add(DividerShadowListItem())

        infoList.forEach {
            items.add(CloseableInfoListItem(it))
        }
        if (infoList.isNotEmpty()) {
            items.add(DividerShadowListItem())
        }


        newItems.forEach { group ->
            items.addAll(group.map { MenuListItem(MenuMapper.mapToDrawer(it)) })
            if (group.isNotEmpty()) {
                items.add(DividerShadowListItem())
            }
        }

        notifyDataSetChanged()
    }

    private fun getMenu(): List<AppMenuItem> = items
        .filterIsInstance<MenuListItem>()
        .filter { MenuRepository.GROUP_MAIN.contains(it.menuItem.appItem.id) }
        .map { it.menuItem.appItem }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        menuSequenceListener.invoke(getMenu())
    }
}