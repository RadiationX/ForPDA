package forpdateam.ru.forpda.ui.fragments.history

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.ui.fragments.history.HistoryAdapter.HistoryHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 06.09.17.
 */
class HistoryAdapter : BaseAdapter<HistoryItem, HistoryHolder>() {
    private var itemClickListener: OnItemClickListener<HistoryItem>? = null

    fun setItemClickListener(itemClickListener: OnItemClickListener<HistoryItem>?) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        return HistoryHolder(inflateLayout(parent, R.layout.item_history))
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        holder.bind(getItem(position), position)
    }


    inner class HistoryHolder(itemView: View) : BaseViewHolder<HistoryItem>(itemView) {
        private val title: TextView = itemView.findViewById(R.id.item_title)
        private val date: TextView = itemView.findViewById(R.id.item_date)

        init {
            itemView.setOnClickListener { v: View? ->
                if (itemClickListener != null) {
                    itemClickListener!!.onItemClick(getItem(layoutPosition))
                }
            }
            itemView.setOnLongClickListener { v: View? ->
                if (itemClickListener != null) {
                    itemClickListener!!.onItemLongClick(getItem(layoutPosition))
                    return@setOnLongClickListener true
                }
                false
            }
        }

        override fun bind(item: HistoryItem, position: Int) {
            title.text = item.title
            date.text = item.date
        }
    }
}
