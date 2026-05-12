package forpdateam.ru.forpda.ui.fragments.notes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ItemNoteBinding
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.NoteListItem

class NoteAdapterDelegate(
    private val clickListener: BaseAdapter.OnItemClickListener<NoteItem>
) : AdapterDelegate<List<ListItem>>() {
    override fun isForViewType(items: List<ListItem>, position: Int): Boolean =
        items[position] is NoteListItem

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return NoteHolder(
            LayoutInflater.from(parent.context).inflate(NoteHolder.LAYOUT, parent, false),
            clickListener
        )
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<Any>
    ) {
        val item = items[position] as NoteListItem
        (holder as NoteHolder).bind(item.item)
    }

    class NoteHolder(
        itemView: View,
        private val clickListener: BaseAdapter.OnItemClickListener<NoteItem>
    ) : BaseViewHolder<NoteItem>(itemView) {

        companion object {
            val LAYOUT: Int = R.layout.item_note
        }

        private val binding by viewBinding<ItemNoteBinding>()

        private lateinit var currentItem: NoteItem

        init {
            itemView.setOnClickListener { v: View? ->
                clickListener.onItemClick(currentItem)
            }
            itemView.setOnLongClickListener { v: View? ->
                clickListener.onItemLongClick(currentItem)
                return@setOnLongClickListener true
                false
            }
        }

        override fun bind(item: NoteItem) {
            currentItem = item
            binding.itemTitle.text = item.title
            if (item.content.isNullOrEmpty()) {
                binding.itemContent.visibility = View.GONE
            } else {
                binding.itemContent.visibility = View.VISIBLE
                binding.itemContent.text = item.content
            }
            //date.setText(item.getDate());
        }
    }
}