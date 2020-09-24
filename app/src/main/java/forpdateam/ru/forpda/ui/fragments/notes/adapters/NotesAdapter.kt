package forpdateam.ru.forpda.ui.fragments.notes.adapters

import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.MenuMapper
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.ui.fragments.other.CloseableInfoDelegate
import forpdateam.ru.forpda.ui.fragments.other.DividerShadowItemDelegate
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.drawers.adapters.*
import java.util.*

class NotesAdapter(
        private val noteClickListener: BaseAdapter.OnItemClickListener<NoteItem>,
        private val infoClickListener: (CloseableInfo) -> Unit
) : ListDelegationAdapter<MutableList<ListItem>>() {


    init {
        items = mutableListOf()
        delegatesManager.apply {
            addDelegate(DividerShadowItemDelegate())
            addDelegate(NoteAdapterDelegate(noteClickListener))
            addDelegate(CloseableInfoDelegate(infoClickListener))
        }
    }

    fun bindItems(notes: List<NoteItem>, infoList: List<CloseableInfo>) {
        items.clear()

        items.addAll(infoList.map { CloseableInfoListItem(it) })

        items.addAll(notes.map { NoteListItem(it) })

        notifyDataSetChanged()
    }
}