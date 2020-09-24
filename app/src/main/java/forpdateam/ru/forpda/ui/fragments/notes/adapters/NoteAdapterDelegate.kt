package forpdateam.ru.forpda.ui.fragments.notes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.app.notes.NoteItem
import forpdateam.ru.forpda.ui.fragments.other.CloseableInfoDelegate
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder
import forpdateam.ru.forpda.ui.views.drawers.adapters.CloseableInfoListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.NoteListItem

class NoteAdapterDelegate(
        private val clickListener: BaseAdapter.OnItemClickListener<NoteItem>
) : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is NoteListItem

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return NoteHolder(
                LayoutInflater.from(parent.context).inflate(NoteHolder.LAYOUT, parent, false),
                clickListener
        )
    }

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
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

        private var title: TextView
        private var date: TextView
        private var content: TextView
        private lateinit var currentItem: NoteItem

        init {
            title = itemView.findViewById<View>(R.id.item_title) as TextView
            date = itemView.findViewById<View>(R.id.item_date) as TextView
            content = itemView.findViewById<View>(R.id.item_content) as TextView
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
            title.text = item.title
            if (item.content == null || item.content.isEmpty()) {
                content.visibility = View.GONE
            } else {
                content.visibility = View.VISIBLE
                content.text = item.content
            }
            //date.setText(item.getDate());
        }
    }
}