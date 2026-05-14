package forpdateam.ru.forpda.ui.fragments.history

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ItemHistoryBinding
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.ui.fragments.history.HistoryAdapter.HistoryHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Created by radiationx on 06.09.17.
 */
class HistoryAdapter(
    private val itemClickListener: OnItemClickListener<HistoryItem>
) : BaseAdapter<HistoryItem, HistoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        return HistoryHolder(itemClickListener, inflateLayout(parent, R.layout.item_history))
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class HistoryHolder(
        private val itemClickListener: OnItemClickListener<HistoryItem>,
        itemView: View
    ) : BaseViewHolder<HistoryItem>(itemView) {

        companion object {
            private val dateFormat = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())
        }

        private val binding by viewBinding<ItemHistoryBinding>()

        override fun bind(item: HistoryItem, position: Int) {
            binding.itemTitle.text = item.title
            binding.itemDate.text = dateFormat.format(item.timestamp)
            itemClickListener.attachTo(itemView, item)
        }
    }
}
