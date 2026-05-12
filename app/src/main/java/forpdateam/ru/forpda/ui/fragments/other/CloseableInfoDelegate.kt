package forpdateam.ru.forpda.ui.fragments.other

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ItemCloseableInfoBinding
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.model.CloseableInfoHolder
import forpdateam.ru.forpda.ui.views.drawers.adapters.CloseableInfoListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class CloseableInfoDelegate(
    private val clickListener: (CloseableInfo) -> Unit
) : AdapterDelegate<List<ListItem>>() {

    override fun isForViewType(items: List<ListItem>, position: Int): Boolean =
        items[position] is CloseableInfoListItem

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<Any>
    ) {
        val item = items[position] as CloseableInfoListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_closeable_info, parent, false),
            clickListener
        )

    class ViewHolder(
        val view: View,
        private val closeClickListener: (CloseableInfo) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val binding by viewBinding<ItemCloseableInfoBinding>()

        private lateinit var currentItem: CloseableInfo

        init {
            binding.infoItemClose.setOnClickListener { closeClickListener.invoke(currentItem) }
        }

        fun bind(item: CloseableInfo) {
            currentItem = item
            binding.infoItemTitle.setText(getStringRes(item))
        }

        private fun getStringRes(item: CloseableInfo): Int = when (item.id) {
            CloseableInfoHolder.item_other_menu_drag -> R.string.closeable_info_other_menu_drag
            CloseableInfoHolder.item_notes_sync -> R.string.closeable_info_notes_sync
            else -> R.string.undefined
        }
    }
}
