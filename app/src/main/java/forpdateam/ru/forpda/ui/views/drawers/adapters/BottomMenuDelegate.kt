package forpdateam.ru.forpda.ui.views.drawers.adapters

import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import kotlinx.android.synthetic.main.item_bottom_tab.view.*

class BottomMenuDelegate(private val clickListener: Listener) : AdapterDelegate<MutableList<ListItem>>() {

    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is BottomTabListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as BottomTabListItem
        (holder as ViewHolder).bind(item.item, item.selected)
    }

    override fun onCreateViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bottom_tab, parent, false))

    private inner class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private lateinit var currentItem: DrawerMenuItem

        init {
            view.setOnClickListener { clickListener.onTabClick(currentItem) }
        }

        fun bind(item: DrawerMenuItem, selected: Boolean) {
            this.currentItem = item
            view.apply {
                contentDescription = context.getString(item.title)
                itemBottomMenuIcon.setImageDrawable(ContextCompat.getDrawable(context, item.icon))

                val colorRes = if (selected) App.getColorFromAttr(context, R.attr.colorAccent) else App.getColorFromAttr(context, R.attr.icon_base)
                itemBottomMenuIcon.setColorFilter(
                        colorRes,
                        PorterDuff.Mode.SRC_ATOP
                )

                itemBottomMenuCounter.visibility = if (item.appItem.count > 0) {
                    // This is done that way because of a bug in the support library related to autosizing when width/height=WRAP_CONTENT
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(itemBottomMenuCounter, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
                    itemBottomMenuCounter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    itemBottomMenuCounter.text = item.appItem.count.toString()
                    post { TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(itemBottomMenuCounter, 3, 10, 1, TypedValue.COMPLEX_UNIT_SP) }
                    View.VISIBLE
                } else View.GONE
            }
        }
    }

    interface Listener {
        fun onTabClick(menu: DrawerMenuItem)
    }
}
