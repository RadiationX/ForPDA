package forpdateam.ru.forpda.ui.fragments.other

import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.MenuListItem
import kotlinx.android.synthetic.main.item_other_menu.view.*

class MenuItemDelegate(
        private val clickListener: (DrawerMenuItem) -> Unit
) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean
            = items[position] is MenuListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as MenuListItem
        (holder as ViewHolder).bind(item.menuItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_other_menu, parent, false),
            clickListener
    )

    class ViewHolder(
            val view: View,
            val clickListener: (DrawerMenuItem) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: DrawerMenuItem

        init {
            view.setOnClickListener { clickListener(currentItem) }
        }

        fun getItem() = currentItem

        fun bind(item: DrawerMenuItem) {
            this.currentItem = item
            view.apply {
                otherMenuTitle.setText(item.title)
                otherMenuIcon.setImageDrawable(AppCompatResources.getDrawable(view.context, item.icon))
                otherMenuCounter.text = item.appItem.count.toString()
                otherMenuCounter.visibility = if (item.appItem.count > 0) View.VISIBLE else View.GONE
            }
        }
    }
}
