package forpdateam.ru.forpda.ui.fragments.notes.adapters

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.ui.fragments.other.CloseableInfoDelegate
import forpdateam.ru.forpda.ui.fragments.other.DividerShadowItemDelegate
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.drawers.adapters.CloseableInfoListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.NoteListItem

class NotesAdapter(
    private val noteClickListener: BaseAdapter.OnItemClickListener<NoteItem>,
    private val infoClickListener: (CloseableInfo) -> Unit
) : ListDelegationAdapter<List<ListItem>>() {


    init {
        delegatesManager.apply {
            addDelegate(DividerShadowItemDelegate())
            addDelegate(NoteAdapterDelegate(noteClickListener))
            addDelegate(CloseableInfoDelegate(infoClickListener))
        }
    }

    fun bindItems(notes: List<NoteItem>, infoList: List<CloseableInfo>) {
        val items = mutableListOf<ListItem>()

        items.addAll(infoList.map { CloseableInfoListItem(it) })

        items.addAll(notes.map { NoteListItem(it) })

        this.items = items

        notifyDataSetChanged()
    }
}