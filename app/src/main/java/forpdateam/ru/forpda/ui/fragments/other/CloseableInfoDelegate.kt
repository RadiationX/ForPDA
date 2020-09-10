package forpdateam.ru.forpda.ui.fragments.other

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.model.CloseableInfoHolder
import forpdateam.ru.forpda.ui.views.drawers.adapters.CloseableInfoListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.MenuListItem
import kotlinx.android.synthetic.main.item_closeable_info.view.*

class CloseableInfoDelegate(
        private val clickListener: (CloseableInfo) -> Unit
) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is CloseableInfoListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as CloseableInfoListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_closeable_info, parent, false),
            clickListener
    )

    class ViewHolder(
            val view: View,
            val closeClickListener: (CloseableInfo) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: CloseableInfo

        init {
            view.infoItemClose.setOnClickListener { closeClickListener.invoke(currentItem) }
        }

        fun bind(item: CloseableInfo) {
            currentItem = item
            view.apply {
                infoItemTitle.setText(getStringRes(item))
            }
        }

        private fun getStringRes(item: CloseableInfo): Int = when (item.id) {
            CloseableInfoHolder.item_other_menu_drag-> R.string.closeable_info_other_menu_drag
            else -> R.string.undefined
        }
    }
}
