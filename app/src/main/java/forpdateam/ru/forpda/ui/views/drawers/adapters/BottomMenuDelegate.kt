package forpdateam.ru.forpda.ui.views.drawers.adapters

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import kotlinx.android.synthetic.main.item_bottom_tab.view.*

class BottomMenuDelegate(private val clickListener: Listener) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is BottomTabListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as BottomTabListItem
        (holder as ViewHolder).bind(item.item, item.selected)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bottom_tab, parent, false))

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: DrawerMenuItem

        init {
            view.setOnClickListener { clickListener.onTabClick(currentItem) }
        }

        fun bind(item: DrawerMenuItem, selected: Boolean) {
            this.currentItem = item
            view.apply {
                itemBottomMenuIcon.setImageDrawable(ContextCompat.getDrawable(context, item.icon))

                val colorRes = if (selected) App.getColorFromAttr(context, R.attr.colorAccent) else App.getColorFromAttr(context, R.attr.icon_base)
                itemBottomMenuIcon.setColorFilter(
                        colorRes,
                        PorterDuff.Mode.SRC_ATOP
                )
                itemBottomMenuCounter.text = item.appItem.count.toString()
                itemBottomMenuCounter.visibility = if (item.appItem.count > 0) View.VISIBLE else View.GONE
            }
        }
    }

    interface Listener {
        fun onTabClick(menu: DrawerMenuItem)
    }
}
